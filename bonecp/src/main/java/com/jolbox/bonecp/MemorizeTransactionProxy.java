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

import com.google.common.collect.ImmutableSet;

/**
 * @author Wallace
 *
 */
public class MemorizeTransactionProxy implements InvocationHandler {

	private Object target;
	private ConnectionHandle connectionHandle;
	private List<ReplayLog> replayLog;
	private static final ImmutableSet<String> clearLogConditions = ImmutableSet.of("rollback", "commit", "close"); 


	public static Connection memorize(final Connection target, List<ReplayLog> replayLog, final ConnectionHandle connectionHandle) {

		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class[] {ConnectionProxy.class},
				new MemorizeTransactionProxy(target, replayLog, connectionHandle));
	}

	public static Statement memorize(final Statement target, List<ReplayLog> replayLog, final ConnectionHandle connectionHandle) {
		return (Statement) Proxy.newProxyInstance(
				StatementProxy.class.getClassLoader(),
				new Class[] {StatementProxy.class},
				new MemorizeTransactionProxy(target, replayLog, connectionHandle));
	}

	public static PreparedStatement memorize(final PreparedStatement target, List<ReplayLog> replayLog, final ConnectionHandle connectionHandle) {
		return (PreparedStatement) Proxy.newProxyInstance(
				PreparedStatementProxy.class.getClassLoader(),
				new Class[] {PreparedStatementProxy.class},
				new MemorizeTransactionProxy(target, replayLog, connectionHandle));
	}

	public static CallableStatement memorize(final CallableStatement target, List<ReplayLog> replayLog, final ConnectionHandle connectionHandle) {
		return (CallableStatement) Proxy.newProxyInstance(
				CallableStatementProxy.class.getClassLoader(),
				new Class[] {CallableStatementProxy.class},
				new MemorizeTransactionProxy(target, replayLog, connectionHandle));
	}

	private MemorizeTransactionProxy(Object target, List<ReplayLog> replayLog, ConnectionHandle connectionHandle) {
		this.target = target;
		this.replayLog = replayLog;
		this.connectionHandle = connectionHandle;
	}

	public Object invoke(Object proxy, Method method,
			Object[] args) throws Throwable {

		Object result;
		int allowedFailedAttemptsRemaining = 3;
		do{
			if (this.connectionHandle.recoveryResult != null){
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
			if (!this.connectionHandle.inReplayMode){
				this.replayLog.add(new ReplayLog(this.target, method, args));
			}
			try{
				// swap with proxies to these too.
				if (method.getName().equals("createStatement")){
					result = memorize((Statement)method.invoke(this.target, args), this.replayLog, this.connectionHandle);
				}
				else if (method.getName().equals("prepareStatement")){
					result = memorize((PreparedStatement)method.invoke(this.target, args), this.replayLog, this.connectionHandle);
				}
				else if (method.getName().equals("prepareCall")){
					result = memorize((CallableStatement)method.invoke(this.target, args), this.replayLog, this.connectionHandle);
				}
				else result = method.invoke(this.target, args);
				
				if (!this.connectionHandle.inReplayMode && (this.target instanceof Connection) && clearLogConditions.contains(method.getName())){
					this.replayLog.clear();
					this.connectionHandle.recoveryResult.getReplaceTarget().clear();
				}
				return result;
			} catch (Throwable t){
				List<ReplayLog> oldReplayLog = this.connectionHandle.getReplayLog();
				this.connectionHandle.inReplayMode = true; // stop recording

				this.connectionHandle.markPossiblyBroken(t.getCause()); // this will possible terminate all connections here

				if (this.connectionHandle.isPossiblyBroken()){ // connection is possibly recoverable...  
					// let's try and recover
					try{
						this.connectionHandle.recoveryResult = attemptRecovery(oldReplayLog); // this might also fail
						this.replayLog = oldReplayLog; // markPossiblyBroken will probably destroy our original connection handle
						this.connectionHandle.inReplayMode = false; // start recording again
						return this.connectionHandle.recoveryResult.getResult();
					} catch(Throwable t2){
						allowedFailedAttemptsRemaining--;
						if (allowedFailedAttemptsRemaining == 0){
							throw t; // throw the original exception
						}
					}
				} else{
					throw t.getCause();
				}
			}
		} while(allowedFailedAttemptsRemaining >= 0);
		return null; // never reached
	}

	/**
	 * @param oldReplayLog 
	 * @throws SQLException 
	 * 
	 */
	protected RecoveryResult attemptRecovery(List<ReplayLog> oldReplayLog) throws SQLException{
		RecoveryResult recoveryResult = this.connectionHandle.recoveryResult;
		List<PreparedStatement> prepStatementTarget = new ArrayList<PreparedStatement>();
		List<CallableStatement> callableStatementTarget = new ArrayList<CallableStatement>();
		List<Statement> statementTarget = new ArrayList<Statement>();
		Object result = null;
		
		// this connection is dead
		this.connectionHandle.inReplayMode = true; // don't go in a loop of saving our saved log!
		try{
			this.connectionHandle.internalClose(); // possibly this will trigger some useless log recording but no harm done
		} catch(Throwable t){
			// do nothing - also likely to fail here
		}
		// this connection is never coming back again. Update counters.
		this.connectionHandle.getPool().postDestroyConnection(this.connectionHandle);
		
		ConnectionHandle con = (ConnectionHandle) this.connectionHandle.getPool().getConnection();
		con.logicallyClosed = true;
		this.connectionHandle.setInternalConnection(con.getInternalConnection());
		this.connectionHandle.renewConnection(); // mark it as logically open again
		con.setReplayLog(oldReplayLog);
		con.recoveryResult = this.connectionHandle.recoveryResult;
		con.inReplayMode = true; // don't go in a loop of saving our saved log!
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
