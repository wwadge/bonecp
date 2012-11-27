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

import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;

/** A connection strategy that is optimized to store/retrieve the connection inside a thread
 * local variable. This makes getting a connection in a managed thread environment such as Tomcat
 * very fast but has the limitation that it only works if # of threads <= # of connections. Should
 * it detect that this isn't the case anymore, this class will flip back permanently to the configured
 * fallback strategy (i.e. default strategy) and makes sure that currently assigned and unused
 * connections are taken back.
 * 
 * @author wallacew
 *
 */
public class CachedConnectionStrategy extends AbstractConnectionStrategy {
	/**  uid */
	private static final long serialVersionUID = -4725640468699097218L;

	/** Logger class. */
	private static final Logger logger = LoggerFactory.getLogger(CachedConnectionStrategy.class);

	/** Just to give out a warning once. */
	private volatile AtomicBoolean warnApp = new AtomicBoolean();
	/** Keep track of connections tied to thread. */
	final protected Map<ConnectionHandle, Reference<Thread>> threadFinalizableRefs = new ConcurrentHashMap<ConnectionHandle, Reference<Thread>>();
	/** Keep track of connections tied to thread. */
	private FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
	/** Obtain connections using this fallback strategy at first (or if this strategy cannot
	 * succeed.
	 */
	private ConnectionStrategy fallbackStrategy;
	 
	/** Connections are stored here. */
	protected CachedConnectionStrategyThreadLocal<SimpleEntry<ConnectionHandle, Boolean>> tlConnections;
	
	/**
	 * @param defaultConnectionStrategy 
	 * @param boneCP 
	 */
	public CachedConnectionStrategy(BoneCP pool, ConnectionStrategy fallbackStrategy){ 
		 this.pool = pool;
		 this.fallbackStrategy = fallbackStrategy; 
		 tlConnections = new CachedConnectionStrategyThreadLocal<SimpleEntry<ConnectionHandle, Boolean>>(this, this.fallbackStrategy); 
	}
	
	
	
	
	/**
	 * Tries to close off all the unused assigned connections back to the pool. Assumes that
	 * the strategy mode has already been flipped prior to calling this routine.
	 * Called whenever our no of connection requests > no of threads. 
	 */
	protected synchronized void stealExistingAllocations(){
		
		for (ConnectionHandle handle: this.threadFinalizableRefs.keySet()){
			// if they're not in use, pretend they are in use now and close them off.
			// this method assumes that the strategy has been flipped back to non-caching mode
			// prior to this method invocation.
			if (handle.logicallyClosed.compareAndSet(true, false)){ 
				try {
					this.pool.releaseConnection(handle);
				} catch (SQLException e) {
					logger.error("Error releasing connection", e);
				}
			}
		}
		if (this.warnApp.compareAndSet(false, true)){ // only issue warning once.
			logger.warn("Cached strategy chosen, but more threads are requesting a connection than are configured. Switching permanently to default strategy.");
		}
		this.threadFinalizableRefs.clear();
		
	}
	
	/** Keep track of this handle tied to which thread so that if the thread is terminated
	 * we can reclaim our connection handle.
	 * @param c connection handle to track.
	 */
	protected void threadWatch(final ConnectionHandle c) {
		this.threadFinalizableRefs.put(c, new FinalizableWeakReference<Thread>(Thread.currentThread(), this.finalizableRefQueue) {
			public void finalizeReferent() {
					try {
						if (!CachedConnectionStrategy.this.pool.poolShuttingDown){
							logger.debug("Monitored thread is dead, closing off allocated connection.");
						}
						c.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					CachedConnectionStrategy.this.threadFinalizableRefs.remove(c);
			}
		});
	}

	@Override
	protected Connection getConnectionInternal() throws SQLException {
		// try to get the connection from thread local storage.
		SimpleEntry<ConnectionHandle, Boolean> result = this.tlConnections.get();
		// we should always be successful. If not, it means we have more threads asking
		// us for a connection than we've got available. This is not supported so we flip
		// back our strategy.
		if (result == null){
			this.pool.cachedPoolStrategy = false;
			this.pool.connectionStrategy = this.fallbackStrategy;
			stealExistingAllocations();
			// get a connection as if under our fallback strategy now.
			return (ConnectionHandle) this.pool.connectionStrategy.getConnection();
		}
		
		return result.getKey();
	}

	@Override
	public ConnectionHandle pollConnection() {
		throw new UnsupportedOperationException();
	}



	public void terminateAllConnections() {
		for (ConnectionHandle conn : this.threadFinalizableRefs.keySet()){
			this.pool.destroyConnection(conn);
		}
		this.threadFinalizableRefs.clear();
		
		this.fallbackStrategy.terminateAllConnections();
	}
	
	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.AbstractConnectionStrategy#cleanupConnection(com.jolbox.bonecp.ConnectionHandle)
	 */
	@Override
	public void cleanupConnection(ConnectionHandle oldHandle, ConnectionHandle newHandle) {
		this.threadFinalizableRefs.remove(oldHandle);
		this.threadWatch(newHandle);
	}



	
	
}
