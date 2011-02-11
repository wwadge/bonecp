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

/**
 * 
 */
package com.jolbox.bonecp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread that monitors another thread and displays stack trace if getConnection() was called without the corresponding close().
 * For debug purposes only. 
 * @author Wallace
 *
 */
public class CloseThreadMonitor implements Runnable {

	/** Handle to the connection we are monitoring.	 */
	private ConnectionHandle connectionHandle;
	/** Location where getConnection() was called. */
	private String stackTrace;
	/** Thread to wait for termination. */
	private Thread threadToMonitor;
	/** ms to wait for thread.join() */
	private long closeConnectionWatchTimeout;
	/** Logger class. */
	private static Logger logger = LoggerFactory.getLogger(CloseThreadMonitor.class);


	/**
	 * @param threadToMonitor Thread to wait for termination
	 * @param connectionHandle connection handle that we are monitoring
	 * @param stackTrace where the getConnection() request started off.
	 * @param closeConnectionWatchTimeout no of ms to wait in thread.join(). 0 = wait forever
	 */
	public CloseThreadMonitor(Thread threadToMonitor, ConnectionHandle connectionHandle, String stackTrace, long closeConnectionWatchTimeout) {
		this.connectionHandle = connectionHandle;
		this.stackTrace = stackTrace;
		this.threadToMonitor = threadToMonitor;
		this.closeConnectionWatchTimeout = closeConnectionWatchTimeout;
	}

	/** {@inheritDoc}
	 * @see java.lang.Runnable#run()
	 */
//	@Override
	public void run() {
		try {
			this.connectionHandle.setThreadWatch(Thread.currentThread());
			// wait for the thread we're monitoring to die off.
			this.threadToMonitor.join(this.closeConnectionWatchTimeout);
			if (!this.connectionHandle.isClosed() 
					&& this.threadToMonitor.equals(this.connectionHandle.getThreadUsingConnection())
				){
				logger.error(this.stackTrace);
			}
		} catch (Exception e) {
			// just kill off this thread
			if (this.connectionHandle != null){ // safety
				this.connectionHandle.setThreadWatch(null);
			}
		} 
	}

}