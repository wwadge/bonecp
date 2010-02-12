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

import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically sends a keep-alive statement to idle threads
 * and kills off any connections that have been unused for a long time (or broken).
 * @author wwadge
 *
 */
public class ConnectionTesterThread implements Runnable {

	/** Connections used less than this time ago are not keep-alive tested. */
	private long idleConnectionTestPeriod;
	/** Max no of ms to wait before a connection that isn't used is killed off. */
	private long idleMaxAge;
	/** Partition being handled. */
	private ConnectionPartition partition;
	/** Scheduler handle. **/
	private ScheduledExecutorService scheduler;
	/** Handle to connection pool. */
    private BoneCP pool;
    /** Logger handle. */
    private static Logger logger = LoggerFactory.getLogger(ConnectionTesterThread.class);
    
	/** Constructor
	 * @param connectionPartition partition to work on
	 * @param scheduler Scheduler handler.
	 * @param pool 
	 */
	public ConnectionTesterThread(ConnectionPartition connectionPartition, ScheduledExecutorService scheduler, BoneCP pool){
		this.partition = connectionPartition;
		this.scheduler = scheduler;
		this.idleMaxAge = pool.getConfig().getIdleMaxAge();
		this.idleConnectionTestPeriod = pool.getConfig().getIdleConnectionTestPeriod();
		this.pool = pool; 
	}


	/** Invoked periodically. */
	public void run() {
		ConnectionHandle connection = null;
	
		
		try {
			int partitionSize= this.partition.getFreeConnections().size();
			for (int i=0; i < partitionSize; i++){
			 
				connection = this.partition.getFreeConnections().poll();
				if (connection != null){
					if (connection.isPossiblyBroken() || 
							((this.idleMaxAge > 0) && (this.partition.getFreeConnections().size() >= this.partition.getMinConnections() && System.currentTimeMillis()-connection.getConnectionLastUsed() > this.idleMaxAge))){
						// kill off this connection
						closeConnection(connection);
						continue;
					}

					if (this.idleConnectionTestPeriod > 0 && (System.currentTimeMillis()-connection.getConnectionLastUsed() > this.idleConnectionTestPeriod) &&
							(System.currentTimeMillis()-connection.getConnectionLastReset() > this.idleConnectionTestPeriod)) {
					   
					    // send a keep-alive, close off connection if we fail.
						if (!this.pool.isConnectionHandleAlive(connection)){
						    closeConnection(connection);
						    continue; 
						}
						connection.setConnectionLastReset(System.currentTimeMillis());
					}

				    this.pool.releaseInAnyFreePartition(connection, this.partition);
				    
					Thread.sleep(20L); // test slowly, this is not an operation that we're in a hurry to deal with...
				}

			} // throw it back on the queue
		} catch (InterruptedException e) {
			if (this.scheduler.isShutdown()){
			    logger.debug("Shutting down connection tester thread.");
				closeConnection(connection);
			} else {
				logger.error("Connection tester thread interrupted", e);
			}
		}
	}


	/** Closes off this connection
	 * @param connection to close
	 */
	private void closeConnection(ConnectionHandle connection) {
		if (connection != null) {
			try {
				connection.internalClose();
				this.pool.postDestroyConnection(connection);
				
			} catch (SQLException e) {
				logger.error("Destroy connection exception", e);
			}
		}
	}

	

}
