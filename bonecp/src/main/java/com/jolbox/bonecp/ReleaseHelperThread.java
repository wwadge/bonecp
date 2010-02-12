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
				if (this.pool.poolShuttingDown){
					// cleanup any remaining stuff. This is a gentle shutdown
					ConnectionHandle connection;
					while ((connection = this.queue.poll()) != null){
						try {
							this.pool.internalReleaseConnection(connection);
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
