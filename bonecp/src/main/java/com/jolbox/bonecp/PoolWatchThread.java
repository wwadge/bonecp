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

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches a partition to create new connections when required.
 * @author wwadge
 *
 */
public class PoolWatchThread implements Runnable {
	/** Partition being monitored. */
	private ConnectionPartition partition;
	/** Pool handle. */
	private BoneCP pool;
	/** Mostly used to break out easily in unit testing. */
	private boolean signalled;
	/** How long to wait before retrying to add a connection upon failure. */
	private long acquireRetryDelay = 1000L;
	/** Start off lazily. */
	private boolean lazyInit;
	/** Occupancy% threshold. */
	private int poolAvailabilityThreshold;
	/** Logger handle. */
	private static Logger logger = LoggerFactory.getLogger(PoolWatchThread.class);


	/** Thread constructor
	 * @param connectionPartition partition to monitor
	 * @param pool Pool handle.
	 */
	public PoolWatchThread(ConnectionPartition connectionPartition, BoneCP pool) {
		this.partition = connectionPartition;
		this.pool = pool;
		this.lazyInit = this.pool.getConfig().isLazyInit();
		this.acquireRetryDelay = this.pool.getConfig().getAcquireRetryDelay();
		this.poolAvailabilityThreshold = this.pool.getConfig().getPoolAvailabilityThreshold();
	}


	public void run() {
		int maxNewConnections;
		while (!this.signalled){
			maxNewConnections=0;

			try{
				if (this.lazyInit){ // block the first time if this is on.
					this.lazyInit = false; 
					this.partition.getPoolWatchThreadSignalQueue().take();
				}
 

				maxNewConnections = this.partition.getMaxConnections()-this.partition.getCreatedConnections();
				// loop for spurious interrupt
				while (maxNewConnections == 0 || (this.partition.getAvailableConnections() *100/this.partition.getMaxConnections() > this.poolAvailabilityThreshold)){
					if (maxNewConnections == 0){
						this.partition.setUnableToCreateMoreTransactions(true);
					}
					this.partition.getPoolWatchThreadSignalQueue().take();
					maxNewConnections = this.partition.getMaxConnections()-this.partition.getCreatedConnections();
				}

				if (maxNewConnections > 0 && !this.lazyInit){
					fillConnections(Math.min(maxNewConnections, this.partition.getAcquireIncrement()));
				}


			} catch (InterruptedException e) {
				return; // we've been asked to terminate.
			}
		}
	}



	/** Adds new connections to the partition.
	 * @param connectionsToCreate number of connections to create
	 * @throws InterruptedException 
	 */
	private void fillConnections(int connectionsToCreate) throws InterruptedException  {
		try {
			for (int i=0; i < connectionsToCreate; i++){
				this.partition.addFreeConnection(new ConnectionHandle(this.partition.getUrl(), this.partition.getUsername(), this.partition.getPassword(), this.pool));
			}
		} catch (SQLException e) {
			logger.error("Error in trying to obtain a connection. Retrying in "+this.acquireRetryDelay+"ms", e);
			Thread.sleep(this.acquireRetryDelay);
		}

	}

}