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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically checks for connections to see if the connection has expired.
 * @author wwadge
 *
 */
public class ConnectionMaxAgeThread implements Runnable {

	/** Partition being handled. */
	private ConnectionPartition partition;
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
	 * @param maxAgeInMs Threads older than this are killed off 
	 * @param lifoMode if true, we're running under a lifo fashion.
	 */
	protected ConnectionMaxAgeThread(ConnectionPartition connectionPartition,  
			BoneCP pool, boolean lifoMode){
		this.partition = connectionPartition;
		this.pool = pool;
		this.lifoMode = lifoMode;
	}


	/** Invoked periodically. */
	public void run() {
		ConnectionHandle connection = null;

		int partitionSize= this.partition.getAvailableConnections();
		long currentTime = System.currentTimeMillis();
		for (int i=0; i < partitionSize; i++){
			try {
				connection = this.partition.getFreeConnections().poll();

				if (connection != null){
					connection.setOriginatingPartition(this.partition);

					if (connection.isExpired(currentTime)){
						// kill off this connection
						closeConnection(connection);
						continue;
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
			}  catch (Throwable e) {
					logger.error("Connection max age thread exception.", e);
			}

		} // throw it back on the queue

	}


	/** Closes off this connection
	 * @param connection to close
	 */
	protected void closeConnection(ConnectionHandle connection) {
		if (connection != null) {
			try {
				connection.internalClose();
			} catch (Throwable t) {
				logger.error("Destroy connection exception", t);
			} finally {
				this.pool.postDestroyConnection(connection);
			}
		}
}
}
