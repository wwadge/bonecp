package com.jolbox.bonecp;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;


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
    private static Logger logger = Logger.getLogger(CustomThreadFactory.class);

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
