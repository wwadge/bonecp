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

import java.sql.SQLException;
import java.util.Date;
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
	/** If true, we're operating in a LIFO fashion. */ 
	private boolean lifoMode;
	/** Logger handle. */
	private static Logger logger = LoggerFactory.getLogger(ConnectionTesterThread.class);

	/** Constructor
	 * @param connectionPartition partition to work on
	 * @param scheduler Scheduler handler.
	 * @param pool pool handle
	 * @param idleMaxAge Threads older than this are killed off 
	 * @param idleConnectionTestPeriod Threads that are idle for more than this time are sent a keep-alive.
	 * @param lifoMode if true, we're running under a lifo fashion.
	 */
	protected ConnectionTesterThread(ConnectionPartition connectionPartition, ScheduledExecutorService scheduler, 
			BoneCP pool, long idleMaxAge, long idleConnectionTestPeriod, boolean lifoMode){
		this.partition = connectionPartition;
		this.scheduler = scheduler;
		this.idleMaxAge = idleMaxAge;
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
		this.pool = pool;
		this.lifoMode = lifoMode;
	}


	/** Invoked periodically. */
	public void run() {
		ConnectionHandle connection = null;
		long tmp;
		try {
				long nextCheck = this.idleConnectionTestPeriod;
				if (this.idleMaxAge > 0){
					if (this.idleConnectionTestPeriod == 0){
						nextCheck = this.idleMaxAge;
					} else {
						nextCheck = Math.min(nextCheck, this.idleMaxAge);
					}
				}
				
				int partitionSize= this.partition.getAvailableConnections();
				long currentTime = System.currentTimeMillis();
				// go thru all partitions
				for (int i=0; i < partitionSize; i++){
					// grab connections one by one.
					connection = this.partition.getFreeConnections().poll();
					if (connection != null){
						connection.setOriginatingPartition(this.partition);
						
						// check if connection has been idle for too long (or is marked as broken)
						if (connection.isPossiblyBroken() || 
								((this.idleMaxAge > 0) && (this.partition.getAvailableConnections() >= this.partition.getMinConnections() && System.currentTimeMillis()-connection.getConnectionLastUsed() > this.idleMaxAge))){
							// kill off this connection - it's broken or it has been idle for too long
							closeConnection(connection);
							continue;
						}
						
						// check if it's time to send a new keep-alive test statement.
						if (this.idleConnectionTestPeriod > 0 && (currentTime-connection.getConnectionLastUsed() > this.idleConnectionTestPeriod) &&
								(currentTime-connection.getConnectionLastReset() >= this.idleConnectionTestPeriod)) {
							// send a keep-alive, close off connection if we fail.
							if (!this.pool.isConnectionHandleAlive(connection)){
								closeConnection(connection);
								continue; 
							}
							// calculate the next time to wake up
							tmp = this.idleConnectionTestPeriod;
							if (this.idleMaxAge > 0){ // wake up earlier for the idleMaxAge test?
								tmp = Math.min(tmp, this.idleMaxAge);
							}
						} else {
							// determine the next time to wake up (connection test time or idle Max age?) 
							tmp = this.idleConnectionTestPeriod-(currentTime - connection.getConnectionLastReset());
							long tmp2 = this.idleMaxAge - (currentTime-connection.getConnectionLastUsed());
							if (this.idleMaxAge > 0){
								tmp = Math.min(tmp, tmp2);
							}
							
						}
						if (tmp < nextCheck){
							nextCheck = tmp; 
						}
						
						if (this.lifoMode){
							// we can't put it back normally or it will end up in front again.
							if (!((LIFOQueue<ConnectionHandle>)connection.getOriginatingPartition().getFreeConnections()).offerLast(connection)){
								connection.internalClose();
							}
						} else {
							this.pool.putConnectionBackInPartition(connection);
						}

						Thread.sleep(20L); // test slowly, this is not an operation that we're in a hurry to deal with (avoid CPU spikes)...
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
			} catch (SQLException e) {
				logger.error("Destroy connection exception", e);
			} finally {
				this.pool.postDestroyConnection(connection);
			}
		}
	}



}
