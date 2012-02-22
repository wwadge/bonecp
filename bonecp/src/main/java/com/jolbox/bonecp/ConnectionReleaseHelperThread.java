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
import java.util.concurrent.BlockingQueue;


/**
 * A thread that monitors a queue containing connections to be released and moves those
 * connections to the partition queue.
 *
 * @author wallacew
 */
public class ConnectionReleaseHelperThread
implements Runnable {

	/** Queue of connections awaiting to be released back to each partition. */
	private BlockingQueue<ConnectionHandle> queue;
	/** Handle to the connection pool. */
	private BoneCP pool;
	/**
	 * Helper Thread constructor.
	 *
	 * @param queue Handle to the release queue.
	 * @param pool handle to the connection pool.
	 */
	public ConnectionReleaseHelperThread(BlockingQueue<ConnectionHandle> queue, BoneCP pool ){
		this.queue = queue;
		this.pool = pool;
	}
	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Runnable#run()
	 */
//	@Override
	public void run() {
		boolean interrupted = false;
		while (!interrupted) {
			try {
				ConnectionHandle connection = this.queue.take();
				this.pool.internalReleaseConnection(connection);
			} catch (SQLException e) {
				interrupted = true;
			} catch (InterruptedException e) {
				if (this.pool.poolShuttingDown){
					// cleanup any remaining stuff. This is a gentle shutdown
					ConnectionHandle connection;
					while ((connection = this.queue.poll()) != null){
						try {
							this.pool.internalReleaseConnection(connection);
						} catch (Exception e1) {
							// yeah we're shutting down, shut up for a bit...
						}
					}
					
				}
					interrupted = true;
			}
		}
	}

}
