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
import java.util.concurrent.TimeUnit;

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
	 * @param pool pool handle
	 * @param idleMaxAge Threads older than this are killed off 
	 * @param idleConnectionTestPeriod Threads that are idle for more than this time are sent a keep-alive.
	 */
	protected ConnectionTesterThread(ConnectionPartition connectionPartition, ScheduledExecutorService scheduler, 
			BoneCP pool, long idleMaxAge, long idleConnectionTestPeriod){
		this.partition = connectionPartition;
		this.scheduler = scheduler;
		this.idleMaxAge = idleMaxAge;
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
		this.pool = pool;
	}


	/** Invoked periodically. */
	public void run() {
		ConnectionHandle connection = null;
		long tmp;
		try {
				long nextCheck = this.idleConnectionTestPeriod;
				
				int partitionSize= this.partition.getAvailableConnections();
				long currentTime = System.currentTimeMillis();
				for (int i=0; i < partitionSize; i++){

					connection = this.partition.getFreeConnections().poll();
					if (connection != null){
						connection.setOriginatingPartition(this.partition);
						if (connection.isPossiblyBroken() || 
								((this.idleMaxAge > 0) && (this.partition.getAvailableConnections() >= this.partition.getMinConnections() && System.currentTimeMillis()-connection.getConnectionLastUsed() > this.idleMaxAge))){
							// kill off this connection
							closeConnection(connection);
							continue;
						}

						if (this.idleConnectionTestPeriod > 0 && (currentTime-connection.getConnectionLastUsed() > this.idleConnectionTestPeriod) &&
								(currentTime-connection.getConnectionLastReset() > this.idleConnectionTestPeriod)) {
							// send a keep-alive, close off connection if we fail.
							if (!this.pool.isConnectionHandleAlive(connection)){
								closeConnection(connection);
								continue; 
							}
							connection.setConnectionLastReset(System.currentTimeMillis());
							tmp = this.idleConnectionTestPeriod;
						} else {
							tmp = this.idleConnectionTestPeriod-(System.currentTimeMillis() - connection.getConnectionLastReset()); 
						}
						if (tmp < nextCheck){
							nextCheck = tmp; 
						}
						
						this.pool.putConnectionBackInPartition(connection);

						Thread.sleep(20L); // test slowly, this is not an operation that we're in a hurry to deal with...
					}

				} // throw it back on the queue

				this.scheduler.schedule(this, nextCheck, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (this.scheduler.isShutdown()){
				logger.debug("Shutting down connection tester thread.");
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
