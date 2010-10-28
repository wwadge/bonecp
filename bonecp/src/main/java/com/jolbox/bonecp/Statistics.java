package com.jolbox.bonecp;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wallacew
 *
 */
public class Statistics {
	/** No of cache hits. */
	public static final AtomicLong cacheHits = new AtomicLong(0);
	/** No of cache misses. */
	public static final AtomicLong cacheMiss = new AtomicLong(0);
	/** No of statements cached. */
	public static final AtomicLong statementsCached = new AtomicLong(0);
}
