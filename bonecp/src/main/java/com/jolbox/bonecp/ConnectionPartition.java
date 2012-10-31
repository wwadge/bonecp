/**
 *  Copyright 2010 Wallace Wadge
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.jolbox.bonecp;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.concurrent.TransferQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.FinalizableWeakReference;

/**
 * Connection Partition structure
 * @author wwadge
 *
 */
public class ConnectionPartition implements Serializable{
	/** Serialization UID */
	private static final long serialVersionUID = -7864443421028454573L;
	/** Logger class. */
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPartition.class);
	/**  Connections available to be taken  */
	private TransferQueue<ConnectionHandle> freeConnections;
	/** When connections start running out, add these number of new connections. */
	private final int acquireIncrement;
	/** Minimum number of connections to start off with. */
	private final int minConnections;
	/** Maximum number of connections that will ever be created. */
	private final int maxConnections;
	/** Statistics lock. */
	protected ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();
	/** Number of connections that have been created. */
	private int createdConnections=0;
	/** DB details. */
	private final String url;
	/** DB details. */
	private final String username;
	/** DB details. */
	private final String password;
	/** If set to true, don't bother calling method to attempt to create
	 * more connections because we've hit our limit. 
	 */
	private volatile boolean unableToCreateMoreTransactions=false;
	/** Config setting. */
	private boolean disableTracking;
	/** Signal trigger to pool watch thread. Making it a queue means our signal is persistent. */
	private BlockingQueue<Object> poolWatchThreadSignalQueue = new ArrayBlockingQueue<Object>(1);
	/** Store the unit translation here to avoid recalculating it in statement handles. */
	private long queryExecuteTimeLimitInNanoSeconds;
	/** Cached copy of the config-specified pool name. */
	private String poolName;
	/** Handle to the pool. */
	protected BoneCP pool;



	/** Returns a handle to the poolWatchThreadSignalQueue
	 * @return the poolWatchThreadSignal
	 */
	protected BlockingQueue<Object> getPoolWatchThreadSignalQueue() {
		return this.poolWatchThreadSignalQueue;
	}

	/** Updates leased connections statistics
	 * @param increment value to add/subtract
	 */
	protected void updateCreatedConnections(int increment) {

		try{
			this.statsLock.writeLock().lock();
			this.createdConnections+=increment;
	//		assert this.createdConnections >= 0 : "Created connections < 0!";
			
		} finally { 
			this.statsLock.writeLock().unlock();
		}
	}

	/**
	 * Adds a free connection.
	 *
	 * @param connectionHandle
	 * @throws SQLException on error
	 */
	protected void addFreeConnection(ConnectionHandle connectionHandle) throws SQLException{
		connectionHandle.setOriginatingPartition(this);
		// assume success to avoid racing where we insert an item in a queue and having that item immediately
		// taken and closed off thus decrementing the created connection count.
		updateCreatedConnections(1);
		if (!this.disableTracking){
			trackConnectionFinalizer(connectionHandle); 
		}
		
		// the instant the following line is executed, consumers can start making use of this 
		// connection.
		if (!this.freeConnections.offer(connectionHandle)){
			// we failed. rollback.
			updateCreatedConnections(-1); // compensate our createdConnection count.
			
			if (!this.disableTracking){
				this.pool.getFinalizableRefs().remove(connectionHandle.getInternalConnection());
			}
			// terminate the internal handle.
			connectionHandle.internalClose();
		}
	}

	/** This method is a replacement for finalize() but avoids all its pitfalls (see Joshua Bloch et. all).
	 * 
	 * Keeps a handle on the connection. If the application called closed, then it means that the handle gets pushed back to the connection
	 * pool and thus we get a strong reference again. If the application forgot to call close() and subsequently lost the strong reference to it,
	 * the handle becomes eligible to garbage connection and thus the the finalizeReferent method kicks in to safely close off the database
	 * handle. Note that we do not return the connectionHandle back to the pool since that is not possible (for otherwise the GC would not 
	 * have kicked in), but we merely safely release the database internal handle and update our counters instead.
	 * @param connectionHandle handle to watch
	 */ 
	protected void trackConnectionFinalizer(ConnectionHandle connectionHandle) {
		if (!this.disableTracking){
		//	assert !connectionHandle.getPool().getFinalizableRefs().containsKey(connectionHandle) : "Already tracking this handle";
			Connection con = connectionHandle.getInternalConnection();
			if (con != null && con instanceof Proxy && Proxy.getInvocationHandler(con) instanceof MemorizeTransactionProxy){
				try {
					// if this is a proxy, get the correct target so that when we call close we're actually calling close on the database
					// handle and not a proxy-based close.
					con = (Connection) Proxy.getInvocationHandler(con).invoke(con, ConnectionHandle.class.getMethod("getProxyTarget"), null);
				} catch (Throwable t) {
					logger.error("Error while attempting to track internal db connection", t); // should never happen
				}
			}
			final Connection internalDBConnection = con;
			final BoneCP pool = connectionHandle.getPool();
			connectionHandle.getPool().getFinalizableRefs().put(internalDBConnection, new FinalizableWeakReference<ConnectionHandle>(connectionHandle, connectionHandle.getPool().getFinalizableRefQueue()) {
				@SuppressWarnings("synthetic-access")
				public void finalizeReferent() {
					try {
						pool.getFinalizableRefs().remove(internalDBConnection);
						if (internalDBConnection != null && !internalDBConnection.isClosed()){ // safety!
							
							logger.warn("BoneCP detected an unclosed connection "+ConnectionPartition.this.poolName + "and will now attempt to close it for you. " +
							"You should be closing this connection in your application - enable connectionWatch for additional debugging assistance or set disableConnectionTracking to true to disable this feature entirely.");
							internalDBConnection.close();
							updateCreatedConnections(-1);
						}
					} catch (Throwable t) {
						logger.error("Error while closing off internal db connection", t);
					}
				}
			});
		}
	}

	/**
	 * @return the freeConnections
	 */
	protected TransferQueue<ConnectionHandle> getFreeConnections() {
		return this.freeConnections;
	}

	/**
	 * @param freeConnections the freeConnections to set
	 */
	protected void setFreeConnections(TransferQueue<ConnectionHandle> freeConnections) {
		this.freeConnections = freeConnections;
	}


	/**
	 * Partition constructor
	 *
	 * @param pool handle to connection pool
	 */
	public ConnectionPartition(BoneCP pool) {
		BoneCPConfig config = pool.getConfig();
		this.minConnections = config.getMinConnectionsPerPartition();
		this.maxConnections = config.getMaxConnectionsPerPartition();
		this.acquireIncrement = config.getAcquireIncrement();
		this.url = config.getJdbcUrl();
		this.username = config.getUsername();
		this.password = config.getPassword();
		this.poolName = config.getPoolName() != null ? "(in pool '"+config.getPoolName()+"') " : "";
		this.pool = pool;
		
		this.disableTracking = config.isDisableConnectionTracking();
		this.queryExecuteTimeLimitInNanoSeconds = TimeUnit.NANOSECONDS.convert(config.getQueryExecuteTimeLimitInMs(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @return the acquireIncrement
	 */
	protected int getAcquireIncrement() {
		return this.acquireIncrement;
	}

	/**
	 * @return the minConnections
	 */
	protected int getMinConnections() {
		return this.minConnections;
	}


	/**
	 * @return the maxConnections
	 */
	protected int getMaxConnections() {
		return this.maxConnections;
	}

	/**
	 * @return the leasedConnections
	 */
	protected int getCreatedConnections() {
		try{
			this.statsLock.readLock().lock();
			return this.createdConnections;
		} finally {
			this.statsLock.readLock().unlock();
		}
	}

	/**
	 * @return the url
	 */
	protected String getUrl() {
		return this.url;
	}


	/**
	 * @return the username
	 */
	protected String getUsername() {
		return this.username;
	}


	/**
	 * @return the password
	 */
	protected String getPassword() {
		return this.password;
	}


	/**
	 * Returns true if we have created all the connections we can
	 *
	 * @return true if we have created all the connections we can
	 */
	protected boolean isUnableToCreateMoreTransactions() {
		return this.unableToCreateMoreTransactions;
	}


	/**
	 * Sets connection creation possible status 
	 *
	 * @param unableToCreateMoreTransactions t/f
	 */
	protected void setUnableToCreateMoreTransactions(boolean unableToCreateMoreTransactions) {
		this.unableToCreateMoreTransactions = unableToCreateMoreTransactions;
	}


	/** Returns the number of avail connections
	 * @return avail connections.
	 */
	protected int getAvailableConnections() {
		return this.freeConnections.size();
	}

	/** Returns no of free slots.
	 * @return remaining capacity.
	 */
	public int getRemainingCapacity() {
		return this.freeConnections.remainingCapacity();
	}

	/** Store the unit translation here to avoid recalculating it in the constructor of StatementHandle. 
	 * @return value
	 */
	protected long getQueryExecuteTimeLimitinNanoSeconds(){
		return this.queryExecuteTimeLimitInNanoSeconds;
	}
}