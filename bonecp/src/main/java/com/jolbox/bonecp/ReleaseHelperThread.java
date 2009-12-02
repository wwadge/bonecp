package com.jolbox.bonecp;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;


/**
 * A thread that monitors a queue containing connections to be released and moves those
 * connections to the partition queue.
 *
 * @author wallacew
 * @version $Revision$
 */
public class ReleaseHelperThread
implements Runnable {

	/** Queue of connections awaiting to be released back to each partition. */
	private BlockingQueue<ConnectionHandle> queue;
	/** Handle to the connection pool. */
	private BoneCP pool;
	/**
	 * Helper Thread constructor.
	 *
	 * @param queue Handle to the release queue.
	 * @param pool handle to the connection pool.
	 */
	public ReleaseHelperThread(BlockingQueue<ConnectionHandle> queue, BoneCP pool ){
		this.queue = queue;
		this.pool = pool;
	}
	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		boolean interrupted = false;
		while (!interrupted) {
			try {
				ConnectionHandle connection = this.queue.take();
				this.pool.internalReleaseConnection(connection);
			} catch (SQLException e) {
				interrupted = true;
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
	}

}
