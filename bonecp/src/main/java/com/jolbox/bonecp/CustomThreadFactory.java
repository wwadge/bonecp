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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class just for executor service to provide a fancier name for debugging + catch for thread exceptions.
 *
 * @author wallacew
 */
public class CustomThreadFactory
        implements ThreadFactory, UncaughtExceptionHandler {

	/** Daemon state. */
    private boolean daemon;
    /** Thread name. */
    private String threadName;
    /** Logger handle. */
    private static final Logger logger = LoggerFactory.getLogger(CustomThreadFactory.class);

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
        Thread t = new Thread(r, this.threadName);
        t.setDaemon(this.daemon);
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
