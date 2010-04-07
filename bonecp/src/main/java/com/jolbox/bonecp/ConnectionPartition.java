/*

Copyright 2009 Wallace Wadge

This file is part of BoneCP.

BoneCP is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BoneCP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jolbox.bonecp;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	static final Logger logger = LoggerFactory.getLogger(ConnectionPartition.class);
	/**  Connections available to be taken  */
    private ArrayBlockingQueue<ConnectionHandle> freeConnections;
    /** When connections start running out, add these number of new connections. */
    private final int acquireIncrement;
    /** Minimum number of connections to start off with. */
    private final int minConnections;
    /** Maximum number of connections that will ever be created. */
    private final int maxConnections;
    /** almost full locking. **/
    private Lock almostFullLock = new ReentrantLock();
    /** Statistics lock. */
    private final ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();
    /** Signal mechanism. */
    private final Condition almostFull = this.almostFullLock.newCondition(); 
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
    /** Scratch queue of connections awaiting to be placed back in queue. */
    private ArrayBlockingQueue<ConnectionHandle> connectionsPendingRelease;
    /** Updates leased connections statistics
     * @param increment value to add/subtract
     */
    protected void updateCreatedConnections(int increment) {
        
    	try{
    		this.statsLock.writeLock().lock();
    		this.createdConnections+=increment;
    	} finally { 
    		this.statsLock.writeLock().unlock();
    	}
    }

    /**
     * Adds a free connection.
     *
     * @param connectionHandle
     */
    protected void addFreeConnection(ConnectionHandle connectionHandle){
        updateCreatedConnections(1);
        connectionHandle.setOriginatingPartition(this);
        this.freeConnections.add(connectionHandle);
        trackConnectionFinalizer(connectionHandle);
    }

    /** This method is a replacement for finalize() but avoids all the pitfalls of it (see Joshua Bloch et. all).
     * 
     * Keeps a handle on the connection. If the application called closed, then it means that the handle gets pushed back to the connection
     * pool and thus we get a strong reference again. If the application forgot to call close() and subsequently lost the strong reference to it,
     * the handle becomes eligible to garbage connection and thus the the finalizeReferent method kicks in to safely close off the database
     * handle. Note that we do not return the connectionHandle back to the pool since that is not possible (for otherwise the GC would not 
     * have kicked in), but we merely safely release the database internal handle and update our counters instead.
     * @param connectionHandle handle to watch
     */
    protected void trackConnectionFinalizer(ConnectionHandle connectionHandle) {
    	Connection con = connectionHandle.getInternalConnection();
    	if (con instanceof Proxy){
    		try {
    			// if this is a proxy, get the correct target so that when we call close we're actually calling close on the database
    			// handle and not a proxy-based close.
				con = (Connection) Proxy.getInvocationHandler(con).invoke(con, ConnectionHandle.class.getMethod("getProxyTarget"), null);
			} catch (Throwable t) {
				logger.error("Error while attempting to track internal db connection", t);
			}
    	}
    	final Connection internalDBConnection = con;
		BoneCP.finalizableRefs.add(new FinalizableWeakReference<ConnectionHandle>(connectionHandle, BoneCP.finalizableRefQueue) {
		public void finalizeReferent() {
			try {
				if (internalDBConnection != null){ // safety!
					logger.warn("BoneCP detected an unclosed connection and will now attempt to close it for you. " +
					"You should be closing this connection in your application - enable connectionWatch for additional debugging assistance.");
					internalDBConnection.close();
					updateCreatedConnections(-1);
				}
			} catch (SQLException e) {
				logger.error("Error while closing off internal db connection", e);
			}
		}
		});
	}

	/**
     * @return the freeConnections
     */
    protected ArrayBlockingQueue<ConnectionHandle> getFreeConnections() {
        return this.freeConnections;
    }

    /**
     * @param freeConnections the freeConnections to set
     */
    protected void setFreeConnections(
            ArrayBlockingQueue<ConnectionHandle> freeConnections) {
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
        this.connectionsPendingRelease = new ArrayBlockingQueue<ConnectionHandle>(this.maxConnections);

        /** Create a number of helper threads for connection release. */
        int helperThreads = config.getReleaseHelperThreads();
        if (helperThreads > 0) {
        	
            ExecutorService releaseHelper = Executors.newFixedThreadPool(helperThreads, new CustomThreadFactory("BoneCP-release-thread-helper-thread", true));
            pool.setReleaseHelper(releaseHelper); // keep a handle just in case
            
            for (int i = 0; i < helperThreads; i++) { 
            	// go through pool.getReleaseHelper() rather than releaseHelper directly to aid unit testing (i.e. mocking)
                pool.getReleaseHelper().execute(new ReleaseHelperThread(this.connectionsPendingRelease, pool));
            }
        }
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
        this.statsLock.readLock().lock();
        int result = this.createdConnections;
        this.statsLock.readLock().unlock();
        return result;
    }

    /**
     * Returns the number of free slots in this partition. 
     *
     * @return number of free slots.
     */
    protected int getRemainingCapacity(){
        return this.freeConnections.remainingCapacity();
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
    public boolean isUnableToCreateMoreTransactions() {
        return this.unableToCreateMoreTransactions;
    }


    /**
     * Sets connection creation possible status 
     *
     * @param unableToCreateMoreTransactions t/f
     */
    public void setUnableToCreateMoreTransactions(boolean unableToCreateMoreTransactions) {
        this.unableToCreateMoreTransactions = unableToCreateMoreTransactions;
    }


    /**
     * Gets handle to a release connection handle queue.
     *
     * @return release connection handle queue 
     */
    public ArrayBlockingQueue<ConnectionHandle> getConnectionsPendingRelease() {
        return this.connectionsPendingRelease;
    }

	/**
	 * Locks the almost full lock
	 */
	protected void lockAlmostFullLock() {
		this.almostFullLock.lock();
	}
	
	/**
	 * Unlocks the almost full lock
	 */
	protected void unlockAlmostFullLock() {
		this.almostFullLock.unlock();
	}

	/** Does _not_ loop for spurious interrupts etc
	 * @throws InterruptedException 
	 * 
	 */
	public void almostFullWait() throws InterruptedException {
		this.almostFull.await(); // callees must loop for spurious interrupts
	}

	/**
	 * signal handle
	 */
	public void almostFullSignal() {
		this.almostFull.signal();
	}

}