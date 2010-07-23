package com.jolbox.bonecp;


import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;


/**
 * A thread that monitors a queue containing statements to be closed. 
 *
 * @author Wallace
 */
public class StatementReleaseHelperThread implements Runnable {

	/** Queue of statements awaiting to be released back to each partition. */
	private BlockingQueue<StatementHandle> queue;
	/** Handle to the connection pool. */
	private BoneCP pool;
	/**
	 * Helper Thread constructor.
	 *
	 * @param queue Handle to the release queue.
	 * @param pool handle to the connection pool.
	 */
	public StatementReleaseHelperThread(BlockingQueue<StatementHandle> queue, BoneCP pool ){
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
				StatementHandle statement = this.queue.take();
				statement.closeStatement();
			} catch (SQLException e) {
				interrupted = true;
			} catch (InterruptedException e) {
				if (this.pool.poolShuttingDown){
					// cleanup any remaining stuff. This is a gentle shutdown
					StatementHandle statement;
					while ((statement = this.queue.poll()) != null){
						try {
							statement.closeStatement();
						} catch (Exception e1) {
							// yeah we're shutting down, shut up for a bit...
						}
					}

				}
				interrupted = true;
			}
		}
	}

}
