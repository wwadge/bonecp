package com.jolbox.bonecp;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionCleanThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionCleanThread.class);
	
	private ConnectionPartition partition;
	
	private BoneCP pool;
	
	protected ConnectionCleanThread(ConnectionPartition connectionPartition, BoneCP pool) {
		this.partition = connectionPartition;
		this.pool = pool;
	}
	
	@Override
	public void run() {
		BlockingQueue<ConnectionHandle> freeQueue = null;
		ConnectionHandle connection = null;
		//获得partition的大小
		int partitionSize = this.partition.getAvailableConnections();
		for (int i = 0; i < partitionSize; i++) {
			//得到free连接的queue
			freeQueue = this.partition.getFreeConnections();
			//如果空闲连接大于partition的最小允许连接数,回缩到最小允许连接数
			while (freeQueue.size() > this.partition.getMinConnections()) {
				connection = freeQueue.poll();
				connection.lock();
				closeConnection(connection);
				connection.unlock();
			}
		}
	}
	
	/** Closes off this connection
	 * @param connection to close
	 */
	private void closeConnection(ConnectionHandle connection) {

		if (connection != null && !connection.isClosed()) {
			try {
				connection.internalClose();
			} catch (SQLException e) {
				logger.error("Destroy connection exception", e);
			} finally {
				this.pool.postDestroyConnection(connection);
				connection.getOriginatingPartition().getPoolWatchThreadSignalQueue().offer(new Object()); // item being pushed is not important.
			}
		}
	}

}
