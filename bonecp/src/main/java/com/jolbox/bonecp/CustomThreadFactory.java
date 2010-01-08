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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class just for executor service to provide a fancier name for debugging + catch for thread exceptions.
 *
 * @author wallacew
 * @version $Revision$
 */
public class CustomThreadFactory
        implements ThreadFactory, UncaughtExceptionHandler {

	/** Daemon state. */
    private boolean daemon;
    /** Thread name. */
    private String threadName;
    /** Logger handle. */
    private static Logger logger = LoggerFactory.getLogger(CustomThreadFactory.class);

    /**
     *  Default constructor.
     *
     * @param threadName name for thread.
     * @param daemon set/unset daemon thread 
     */
    public CustomThreadFactory(String threadName, boolean daemon){
        this.threadName = threadName;
        this.daemon = daemon;
    }
    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    //@Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(this.daemon);
        t.setName(this.threadName);
        t.setUncaughtExceptionHandler(this);
        return t;
    }
	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	public void uncaughtException(Thread thread, Throwable throwable) {
		logger.error("Uncaught Exception in thread "+ thread.getName(), throwable);
	}

}
