package com.jolbox.bonecp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Parent class for the different pool strategies.
 * @author wallacew
 *
 */
public abstract class AbstractConnectionStrategy implements ConnectionStrategy{

	/** Pool handle */
	protected BoneCP pool;
	
	/** Prevent repeated termination of all connections when the DB goes down. */
	protected Lock terminationLock = new ReentrantLock();
	
	
	/** Prep for a new connection
	 * @return if stats are enabled, return the nanoTime when this connection was requested.
	 * @throws SQLException
	 */
	protected long preConnection() throws SQLException{
		long statsObtainTime = 0;
		
		if (this.pool.poolShuttingDown){
			throw new SQLException(this.pool.shutdownStackTrace);
		}


		if (this.pool.statisticsEnabled){
			statsObtainTime = System.nanoTime();
			this.pool.statistics.incrementConnectionsRequested();
		}
		
		return statsObtainTime;
	}
	
	
	/** After obtaining a connection, perform additional tasks.
	 * @param handle
	 * @param statsObtainTime
	 */
	protected void postConnection(ConnectionHandle handle, long statsObtainTime){

		handle.renewConnection(); // mark it as being logically "open"

		// Give an application a chance to do something with it.
		if (handle.getConnectionHook() != null){
			handle.getConnectionHook().onCheckOut(handle);
		}

		if (this.pool.closeConnectionWatch){ // a debugging tool
			this.pool.watchConnection(handle);
		}

		if (this.pool.statisticsEnabled){
			this.pool.statistics.addCumulativeConnectionWaitTime(System.nanoTime()-statsObtainTime);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		long statsObtainTime = preConnection();
		
		ConnectionHandle result = (ConnectionHandle) getConnectionInternal();
		
		postConnection(result, statsObtainTime);
		
		return result;
	}

	/** Actual call that returns a connection
	 * @return Connection
	 * @throws SQLException
	 */
	protected abstract Connection getConnectionInternal() throws SQLException;
	
		
	public ConnectionHandle pollConnection(){
		// usually overridden
		return null; 
	}
}
