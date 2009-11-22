package com.jolbox.bonecp;

import java.sql.SQLException;

import org.apache.log4j.Logger;

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
    /** Logger handle. */
    private static  Logger logger = Logger.getLogger(PoolWatchThread.class);
  

	/** Thread constructor
	 * @param connectionPartition partition to monitor
	 * @param pool Pool handle.
	 */
	public PoolWatchThread(ConnectionPartition connectionPartition, BoneCP pool) {
		this.partition = connectionPartition;
		this.pool = pool;
	}


	public void run() {
		int maxNewConnections;
		
		while (!this.signalled){
			maxNewConnections=0;

			try{
				this.partition.lockAlmostFullLock();
				maxNewConnections = this.partition.getMaxConnections()-this.partition.getCreatedConnections();
				// loop for spurious interrupt
				while (maxNewConnections == 0 || (this.partition.getFreeConnections().size()*100/this.partition.getMaxConnections() > BoneCP.HIT_THRESHOLD)){
                    if (maxNewConnections == 0){
                        this.partition.setUnableToCreateMoreTransactions(true);
                    }

					this.partition.almostFullWait();
			 		maxNewConnections = this.partition.getMaxConnections()-this.partition.getCreatedConnections();
				}
			} catch (InterruptedException e) {
//				    logger.debug("Pool Watch scheduler has been shut down. Terminating");
				    return;
			} finally {
				this.partition.unlockAlmostFullLock();
			}
			if (maxNewConnections > 0){
				fillConnections(Math.min(maxNewConnections, this.partition.getAcquireIncrement()));
			} 
		}

	}


	/** Adds new connections to the partition.
 	 * @param connectionsToCreate number of connections to create
	 */
	private void fillConnections(int connectionsToCreate) {
		for (int i=0; i < connectionsToCreate; i++){
			try {
				this.partition.addFreeConnection(new ConnectionHandle(this.partition.getUrl(), this.partition.getUsername(), this.partition.getPassword(), this.pool));
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		
	}

}
