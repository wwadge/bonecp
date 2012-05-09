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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A thread that monitors a queue containing statements to be closed. 
 *
 * @author Wallace
 */
public class StatementReleaseHelperThread implements Runnable {

	/** Queue of statements awaiting to be released back to each partition. */
	private BlockingQueue<StatementHandle> queue;
	/** Handle to the connection pool. */
	private BoneCP pool;
	/** Handle to logger. */
	private static final Logger logger = LoggerFactory.getLogger(StatementReleaseHelperThread.class);

	/**
	 * Helper Thread constructor.
	 *
	 * @param queue Handle to the release queue.
	 * @param pool handle to the connection pool.
	 */
	public StatementReleaseHelperThread(BlockingQueue<StatementHandle> queue, BoneCP pool ){
		this.queue = queue;
		this.pool = pool;
	}
	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean interrupted = false;
		while (!interrupted) {
			try {
				StatementHandle statement = this.queue.take();
//				assert statement.enqueuedForClosure : "Not enqueued!";	
				statement.closeStatement();
			} catch (InterruptedException e) {
				if (this.pool.poolShuttingDown){
					// cleanup any remaining stuff. This is a gentle shutdown
					StatementHandle statement;
					while ((statement = this.queue.poll()) != null){
						try {
							statement.closeStatement();
						} catch (SQLException e1) {
							// yeah we're shutting down, shut up for a bit...
						}
					}

				}
				interrupted = true;
			}			
			catch (Exception e) {
				logger.error("Could not close statement.", e);
			}
		}
	}
}
