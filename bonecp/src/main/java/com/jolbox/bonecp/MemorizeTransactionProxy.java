/**
 * 
 */
package com.jolbox.bonecp;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.jolbox.bonecp.proxy.CallableStatementProxy;
import com.jolbox.bonecp.proxy.ConnectionProxy;
import com.jolbox.bonecp.proxy.PreparedStatementProxy;
import com.jolbox.bonecp.proxy.TransactionRecoveryResult;
import com.jolbox.bonecp.proxy.StatementProxy;

/** This code takes care of recording and playing back of transactions (when a failure occurs). The idea behind this is to wrap a connection
 * or statement with proxies and log all method calls. When a failure occurs, thrash the inner connection, obtain a new one and play back
 * the previously recorded methods. 
 * 
 * @author Wallace
 *
 */
public class MemorizeTransactionProxy implements InvocationHandler {
	/** Target of proxy. */
	private Object target;
	/** Connection handle. */
	private ConnectionHandle connectionHandle;
	/** List of methods that will trigger a reset of the transaction. */
	private static final ImmutableSet<String> clearLogConditions = ImmutableSet.of("rollback", "commit", "close"); 
	/** Class logger. */
	private static final Logger logger = LoggerFactory.getLogger(MemorizeTransactionProxy.class);


	/** Wrap connection with a proxy.
	 * @param target connection handle
	 * @param connectionHandle originating bonecp connection
	 * @return Proxy to a connection.
	 */
	protected static Connection memorize(final Connection target, final ConnectionHandle connectionHandle) {

		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class[] {ConnectionProxy.class},
				new MemorizeTransactionProxy(target, connectionHandle));
	}

	/** Wrap Statement with a proxy.
	 * @param target statement handle
	 * @param connectionHandle originating bonecp connection
	 * @return Proxy to a statement.
	 */
	protected static Statement memorize(final Statement target, final ConnectionHandle connectionHandle) {
		return (Statement) Proxy.newProxyInstance(
				StatementProxy.class.getClassLoader(),
				new Class[] {StatementProxy.class},
				new MemorizeTransactionProxy(target, connectionHandle));
	}

	/** Wrap PreparedStatement with a proxy.
	 * @param target statement handle
	 * @param connectionHandle originating bonecp connection
	 * @return Proxy to a Preparedstatement.
	 */
	protected static PreparedStatement memorize(final PreparedStatement target, final ConnectionHandle connectionHandle) {
		return (PreparedStatement) Proxy.newProxyInstance(
				PreparedStatementProxy.class.getClassLoader(),
				new Class[] {PreparedStatementProxy.class},
				new MemorizeTransactionProxy(target, connectionHandle));
	}


	/** Wrap CallableStatement with a proxy.
	 * @param target statement handle
	 * @param connectionHandle originating bonecp connection
	 * @return Proxy to a Callablestatement.
	 */
	protected static CallableStatement memorize(final CallableStatement target, final ConnectionHandle connectionHandle) {
		return (CallableStatement) Proxy.newProxyInstance(
				CallableStatementProxy.class.getClassLoader(),
				new Class[] {CallableStatementProxy.class},
				new MemorizeTransactionProxy(target, connectionHandle));
	}

	/** Main constructor
	 * @param target target to actual handle
	 * @param connectionHandle bonecp ref
	 */
	private MemorizeTransactionProxy(Object target, ConnectionHandle connectionHandle) {
		this.target = target;
		this.connectionHandle = connectionHandle;
	}

	public Object invoke(Object proxy, Method method,
			Object[] args) throws Throwable {

		Object result;

		if (this.connectionHandle.isInReplayMode()){ // go straight through when flagged as in playback (replay) mode.
			return method.invoke(this.target, args);
		}
		
		if (this.connectionHandle.recoveryResult != null){ // if we previously failed, do the mapping to the new connection/statements
			Object remap = this.connectionHandle.recoveryResult.getReplaceTarget().get(this.target);
			if (remap != null){
				this.target = remap;
			}
			remap = this.connectionHandle.recoveryResult.getReplaceTarget().get(this.connectionHandle);
			if (remap != null){
				this.connectionHandle = (ConnectionHandle) remap;
			}
		}
		
		// record this invocation
		if (!this.connectionHandle.isInReplayMode() && !method.getName().equals("hashCode") && !method.getName().equals("equals") && !method.getName().equals("toString")){
			this.connectionHandle.getReplayLog().add(new ReplayLog(this.target, method, args));
		}
		
		try{
			// swap with proxies to these too.
			if (method.getName().equals("createStatement")){
				result = memorize((Statement)method.invoke(this.target, args), this.connectionHandle);
			}
			else if (method.getName().equals("prepareStatement")){
				result = memorize((PreparedStatement)method.invoke(this.target, args), this.connectionHandle);
			}
			else if (method.getName().equals("prepareCall")){
				result = memorize((CallableStatement)method.invoke(this.target, args), this.connectionHandle);
			}
			else result = method.invoke(this.target, args);

			// when we commit/close/rollback, destroy our log
			if (!this.connectionHandle.isInReplayMode() && (this.target instanceof Connection) && clearLogConditions.contains(method.getName())){
				this.connectionHandle.getReplayLog().clear();
				this.connectionHandle.recoveryResult.getReplaceTarget().clear();
			}
			return result;
		} catch (Throwable t){
			List<ReplayLog> oldReplayLog = this.connectionHandle.getReplayLog();
			this.connectionHandle.setInReplayMode(true); // stop recording

			this.connectionHandle.markPossiblyBroken(t.getCause()); // this will possibly terminate all connections here

			if (this.connectionHandle.isPossiblyBroken()){ // connection is possibly recoverable...
				logger.error("Connection failed. Attempting to recover transaction....");
				// let's try and recover
				try{
					this.connectionHandle.recoveryResult = attemptRecovery(oldReplayLog); // this might also fail
					this.connectionHandle.setReplayLog(oldReplayLog); // markPossiblyBroken will probably destroy our original connection handle
					this.connectionHandle.setInReplayMode(false); // start recording again
					return this.connectionHandle.recoveryResult.getResult();
				} catch(Throwable t2){
					throw new SQLException("Could not recover transaction", t2);
				}
			} 
			
			throw t.getCause();
			
		}
	}

	/** Play back a transaction
	 * @param oldReplayLog 
	 * @return map + result
	 * @throws SQLException 
	 * 
	 */
	private TransactionRecoveryResult attemptRecovery(List<ReplayLog> oldReplayLog) throws SQLException{
		TransactionRecoveryResult recoveryResult = this.connectionHandle.recoveryResult;
		List<PreparedStatement> prepStatementTarget = new ArrayList<PreparedStatement>();
		List<CallableStatement> callableStatementTarget = new ArrayList<CallableStatement>();
		List<Statement> statementTarget = new ArrayList<Statement>();
		Object result = null;

		// this connection is dead
		this.connectionHandle.setInReplayMode(true); // don't go in a loop of saving our saved log!
		try{
			this.connectionHandle.clearStatementCaches(true);
			this.connectionHandle.close();
		} catch(Throwable t){
			// do nothing - also likely to fail here
		}
		this.connectionHandle.setInternalConnection(this.connectionHandle.obtainInternalConnection());

		Map<Object, Object> replaceTarget = recoveryResult.getReplaceTarget();

		for (ReplayLog replay: oldReplayLog){

			// we got new connections/statement handles so replace what we've got with the new ones
			if (replay.getTarget() instanceof Connection){
				replaceTarget.put(replay.getTarget(), this.connectionHandle.getInternalConnection());
			} else if (replay.getTarget() instanceof PreparedStatement){
				if (replaceTarget.get(replay.getTarget()) == null){
					replaceTarget.put(replay.getTarget(), prepStatementTarget.remove(0));
				}
			} else if (replay.getTarget() instanceof CallableStatement){
				if (replaceTarget.get(replay.getTarget()) == null){
					replaceTarget.put(replay.getTarget(), callableStatementTarget.remove(0));
				}
			} else if (replay.getTarget() instanceof Statement){
				if (replaceTarget.get(replay.getTarget()) == null){
					replaceTarget.put(replay.getTarget(), statementTarget.remove(0));
				}
			}

			replay.setTarget(replaceTarget.get(replay.getTarget())); // fix our log

			try {
				// run again using the new connection/statement
				result = replay.getMethod().invoke(replay.getTarget(), replay.getArgs());
				// remember what we've got
				recoveryResult.setResult(result);

				// if we got a new statement (eg a prepareStatement call), save it, we'll use it for our search/replace
				if (result instanceof PreparedStatement){
					prepStatementTarget.add((PreparedStatement)result);
				} else if (result instanceof CallableStatement){
					callableStatementTarget.add((CallableStatement)result);
				} else if (result instanceof Statement){
					statementTarget.add((Statement)result);
				}  
			} catch (Exception e) {
				throw new SQLException(e);
			}
		}
		return recoveryResult;
	}


}
