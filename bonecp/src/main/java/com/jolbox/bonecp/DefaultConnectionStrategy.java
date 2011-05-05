package com.jolbox.bonecp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/** The normal getConnection() strategy class in use. Attempts to get a connection from 
 * one or more configured partitions.
 * @author wallacew
 *
 */
public class DefaultConnectionStrategy extends AbstractConnectionStrategy {

	 /** 
	  * Singleton pattern.
	  */
	private DefaultConnectionStrategy(){ 
		// singleton
	}
	
	 /** Singleton pattern.
	 */
	private static class SingletonHolder { 
		 /** Singleton pattern.
		 */
	     @SuppressWarnings("synthetic-access")
		public static final DefaultConnectionStrategy INSTANCE = new DefaultConnectionStrategy();
	   }

	 
	/** Singleton pattern.
	 * @param pool
	 * @return DefaultConnectionStrategy
	 */
	public static DefaultConnectionStrategy getInstance(BoneCP pool){
		 DefaultConnectionStrategy dcs = SingletonHolder.INSTANCE;
		 dcs.pool = pool;
		 return dcs;
	}
	
	@Override
	public ConnectionHandle pollConnection(){
		ConnectionHandle result = null;

		int partition = (int) (Thread.currentThread().getId() % this.pool.partitionCount);
		ConnectionPartition connectionPartition = this.pool.partitions[partition];

		result = connectionPartition.getFreeConnections().poll();

		if (result == null) { 
			// we ran out of space on this partition, pick another free one
			for (int i=0; i < this.pool.partitionCount; i++){
				if (i == partition) {
					continue; // we already determined it's not here
				}
				result = this.pool.partitions[i].getFreeConnections().poll(); // try our luck with this partition
				connectionPartition = this.pool.partitions[i];
				if (result != null) {
					break;  // we found a connection
				}
			}
		}

		if (!connectionPartition.isUnableToCreateMoreTransactions()){ // unless we can't create any more connections...   
			this.pool.maybeSignalForMoreConnections(connectionPartition);  // see if we need to create more
		}
		
		return result;

	}
	@Override
	protected Connection getConnectionInternal() throws SQLException {
		
		ConnectionHandle result = pollConnection();

		int partition = (int) (Thread.currentThread().getId() % this.pool.partitionCount);
		ConnectionPartition connectionPartition = this.pool.partitions[partition];
		
		// we still didn't find an empty one, wait forever (or as per config) until our partition is free
		if (result == null) {
			try {
				result = connectionPartition.getFreeConnections().poll(this.pool.connectionTimeoutInMs, TimeUnit.MILLISECONDS);
				if (result == null){
					if (this.pool.nullOnConnectionTimeout){
						return null;
					}
					// 08001 = The application requester is unable to establish the connection.
					throw new SQLException("Timed out waiting for a free available connection.", "08001");
				}
			}
			catch (InterruptedException e) {
				if (this.pool.nullOnConnectionTimeout){
					return null;
				}
				throw new SQLException(e.getMessage());
			}
		}
		
		return result;
	}
	
	/** Closes off all connections in all partitions. */
	public void terminateAllConnections(){
		this.terminationLock.lock();
		try{
			ConnectionHandle conn;
				
			// close off all connections.
			for (int i=0; i < this.pool.partitionCount; i++) {
				while ((conn = this.pool.partitions[i].getFreeConnections().poll()) != null){
					this.pool.destroyConnection(conn);
				}

			}
		} finally {
			this.terminationLock.unlock();
		}
	}

}
