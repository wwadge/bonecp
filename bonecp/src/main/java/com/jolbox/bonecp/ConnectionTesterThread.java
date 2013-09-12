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
	private long idleConnectionTestPeriodInMs;
	/** Max no of ms to wait before a connection that isn't used is killed off. */
	private long idleMaxAgeInMs;
	/** Partition being handled. */
	private ConnectionPartition partition;
	/** Scheduler handle. **/
	private ScheduledExecutorService scheduler;
	/** Handle to connection pool. */
	private BoneCP pool;
	/** If true, we're operating in a LIFO fashion. */ 
	private boolean lifoMode;
	/** Logger handle. */
	private static final Logger logger = LoggerFactory.getLogger(ConnectionTesterThread.class);

	/** Constructor
	 * @param connectionPartition partition to work on
	 * @param scheduler Scheduler handler.
	 * @param pool pool handle
	 * @param idleMaxAgeInMs Threads older than this are killed off 
	 * @param idleConnectionTestPeriodInMs Threads that are idle for more than this time are sent a keep-alive.
	 * @param lifoMode if true, we're running under a lifo fashion.
	 */
	protected ConnectionTesterThread(ConnectionPartition connectionPartition, ScheduledExecutorService scheduler, 
			BoneCP pool, long idleMaxAgeInMs, long idleConnectionTestPeriodInMs, boolean lifoMode){
		this.partition = connectionPartition;
		this.scheduler = scheduler;
		this.idleMaxAgeInMs = idleMaxAgeInMs;
		this.idleConnectionTestPeriodInMs = idleConnectionTestPeriodInMs;
		this.pool = pool;
		this.lifoMode = lifoMode;
	}


	/** Invoked periodically. */
	public void run() {
		ConnectionHandle connection = null;
		long tmp;
		try {
				long nextCheckInMs = this.idleConnectionTestPeriodInMs;
				if (this.idleMaxAgeInMs > 0){
					if (this.idleConnectionTestPeriodInMs == 0){
						nextCheckInMs = this.idleMaxAgeInMs;
					} else {
						nextCheckInMs = Math.min(nextCheckInMs, this.idleMaxAgeInMs);
					}
				}
				
				int partitionSize= this.partition.getAvailableConnections();
				long currentTimeInMs = System.currentTimeMillis();
				// go thru all partitions
				for (int i=0; i < partitionSize; i++){
					// grab connections one by one.
					connection = this.partition.getFreeConnections().poll();
					if (connection != null){
						connection.setOriginatingPartition(this.partition);
						
						// check if connection has been idle for too long (or is marked as broken)
						if (connection.isPossiblyBroken() || 
								((this.idleMaxAgeInMs > 0) && ( System.currentTimeMillis()-connection.getConnectionLastUsedInMs() > this.idleMaxAgeInMs))){
							// kill off this connection - it's broken or it has been idle for too long
							closeConnection(connection);
							continue;
						}
						
						// check if it's time to send a new keep-alive test statement.
						if (this.idleConnectionTestPeriodInMs > 0 && (currentTimeInMs-connection.getConnectionLastUsedInMs() > this.idleConnectionTestPeriodInMs) &&
								(currentTimeInMs-connection.getConnectionLastResetInMs() >= this.idleConnectionTestPeriodInMs)) {
							// send a keep-alive, close off connection if we fail.
							if (!this.pool.isConnectionHandleAlive(connection)){
								closeConnection(connection);
								continue; 
							}
							// calculate the next time to wake up
							tmp = this.idleConnectionTestPeriodInMs;
							if (this.idleMaxAgeInMs > 0){ // wake up earlier for the idleMaxAge test?
								tmp = Math.min(tmp, this.idleMaxAgeInMs);
							}
						} else {
							// determine the next time to wake up (connection test time or idle Max age?) 
							tmp = Math.abs(this.idleConnectionTestPeriodInMs-(currentTimeInMs - connection.getConnectionLastResetInMs()));
							long tmp2 = Math.abs(this.idleMaxAgeInMs - (currentTimeInMs-connection.getConnectionLastUsedInMs()));
							if (this.idleMaxAgeInMs > 0){
								tmp = Math.min(tmp, tmp2);
							}
							
						}
						if (tmp < nextCheckInMs){
							nextCheckInMs = tmp; 
						}
						
						if (this.lifoMode){
							// we can't put it back normally or it will end up in front again.
							if (!(connection.getOriginatingPartition().getFreeConnections().offer(connection))){
								connection.internalClose();
							}
						} else {
							this.pool.putConnectionBackInPartition(connection);
						}

						Thread.sleep(20L); // test slowly, this is not an operation that we're in a hurry to deal with (avoid CPU spikes)...
					}

				} // throw it back on the queue
				// offset by a bit to avoid firing a lot for slightly offset connections
//				logger.debug("Next check in "+nextCheckInMs);
				
				this.scheduler.schedule(this, nextCheckInMs, TimeUnit.MILLISECONDS);
		} catch (Throwable e) {
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
	protected void closeConnection(ConnectionHandle connection) {

		if (connection != null && !connection.isClosed()) {
			try {
				connection.internalClose();
			} catch (SQLException e) {
				logger.error("Destroy connection exception", e);
			} finally {
				this.pool.postDestroyConnection(connection);
				connection.getOriginatingPartition().getPoolWatchThreadSignalQueue().offer(new Object()); // item being pushed is not important.
			}
		}
	}



}
