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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistics class.
 * @author wallacew
 *
 */
public class Statistics implements StatisticsMBean{
	/** No of cache hits. */
	private final AtomicLong cacheHits = new AtomicLong(0);
	/** No of cache misses. */
	private final AtomicLong cacheMiss = new AtomicLong(0);
	/** No of statements cached. */
	private final AtomicLong statementsCached = new AtomicLong(0);
	/** Connections obtained. */
	private final AtomicLong connectionsRequested = new AtomicLong(0);
	/** Time taken to give a connection to the application. */  
	private final AtomicLong connectionWaitTime = new AtomicLong(0);
	/** Pool handle. */
	private BoneCP pool;

	/** BoneCP handle.
	 * @param pool
	 */
	public Statistics(BoneCP pool){
		this.pool = pool;
	}
	
	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#resetStats()
	 */
	public void resetStats(){
		this.cacheHits.set(0);
		this.cacheMiss.set(0);
		this.statementsCached.set(0);
		this.connectionsRequested.set(0);
		this.connectionWaitTime.set(0);
	}
	
	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getWaitTimeAvg()
	 */
	public long getWaitTimeAvg(){
		return this.connectionsRequested.get() == 0 ? 0 : Math.round((this.connectionWaitTime.get() / this.connectionsRequested.get()) / 1000000.0);
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getTotalLeased()
	 */
	public int getTotalLeased() {
		return this.pool.getTotalLeased();
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getTotalFree()
	 */
	public int getTotalFree() {
		return this.pool.getTotalFree();
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getTotalCreatedConnections()
	 */
	public int getTotalCreatedConnections() {
		return this.pool.getTotalCreatedConnections();
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getCacheHits()
	 */
	public long getCacheHits() {
		return this.cacheHits.get();
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getCacheMiss()
	 */
	public long getCacheMiss() {
		return this.cacheMiss.get();
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getStatementsCached()
	 */
	public long getStatementsCached() {
		return this.statementsCached.get();
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getConnectionsRequested()
	 */
	public long getConnectionsRequested() {
		return this.connectionsRequested.get();
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.StatisticsMBean#getConnectionWaitTime()
	 */
	public long getConnectionWaitTime() {
		return Math.round(this.connectionWaitTime.get() / 1000000.0);
	}

	/** Adds connection wait time.
	 * @param increment
	 */
	protected void addConnectionWaitTime(long increment) {
		this.connectionWaitTime.addAndGet(increment);
	}

	/**
	 * Accessor method.
	 */
	protected void incrementStatementsCached() {
		this.statementsCached.incrementAndGet();
	}

	/**
	 * Accessor method.
	 */
	protected void incrementCacheMiss() {
		this.cacheMiss.incrementAndGet();
	}


	/**
	 * Accessor method.
	 */
	protected void incrementCacheHits() {
		this.cacheHits.incrementAndGet();
	}

	/**
	 * Accessor method.
	 */
	protected void incrementConnectionsRequested() {
		this.connectionsRequested.incrementAndGet();
	}
}
