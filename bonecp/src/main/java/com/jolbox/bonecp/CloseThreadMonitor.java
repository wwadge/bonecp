/**
 * Copyright 2009 Wallace Wadge
 *
 * This file is part of BoneCP.
 *
 * BoneCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BoneCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
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
			// wait for the thread we're monitoring to die off.
			this.threadToMonitor.join(this.closeConnectionWatchTimeout);
			if (!this.connectionHandle.isClosed() 
					&& this.threadToMonitor.equals(this.connectionHandle.getThreadUsingConnection())
				){
				logger.error(this.stackTrace);
			}
		} catch (Exception e) {
			// just kill off this thread
		} 
	}

}