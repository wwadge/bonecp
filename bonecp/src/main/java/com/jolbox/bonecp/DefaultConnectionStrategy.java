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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** The normal getConnection() strategy class in use. Attempts to get a connection from 
 * one or more configured partitions.
 * @author wallacew
 *
 */
public class DefaultConnectionStrategy extends AbstractConnectionStrategy {

	/** uid */
	private static final long serialVersionUID = 962520166486807512L;

	public DefaultConnectionStrategy(BoneCP pool){
		this.pool = pool;
	}
	
	@Override
  public ConnectionHandle pollConnection(){
    ConnectionHandle result = null;

    int partition = (int) (Thread.currentThread().getId() % this.pool.partitionCount);
    final ConnectionPartition connectionPartition = this.pool.partitions[partition];
    
    result = connectionPartition.getFreeConnections().poll();

    if (result == null) {
      // we ran out of space on this partition, pick another free one
      for (int i=0; i < this.pool.partitionCount; i++){
        if (i == partition) {
          continue; // we already determined it's not here
        }
        result = this.pool.partitions[i].getFreeConnections().poll(); // try our luck with this partition
       
        if (result != null) {
          break;  // we found a connection
        }
      }
    }
		
    if (!connectionPartition.isUnableToCreateMoreTransactions()){ // unless we can't create any more connections...
      this.pool.maybeSignalForMoreConnections(connectionPartition);  // see if we need to create more
    }
		
    return result;

  }

	@Override
	protected Connection getConnectionInternal() throws SQLException {
		
		ConnectionHandle result = pollConnection();
		
		// we still didn't find an empty one, wait forever (or as per config) until our partition is free
		if (result == null) {
			int partition = (int) (Thread.currentThread().getId() % this.pool.partitionCount);
			ConnectionPartition connectionPartition = this.pool.partitions[partition];

			try {
				result = connectionPartition.getFreeConnections().poll(this.pool.connectionTimeoutInMs, TimeUnit.MILLISECONDS);
				if (result == null){
					if (this.pool.nullOnConnectionTimeout){
						return null;
					}
					// 08001 = The application requester is unable to establish the connection.
					throw new SQLException("Timed out waiting for a free available connection.", "08001");
				}
			}
			catch (InterruptedException e) {
				if (this.pool.nullOnConnectionTimeout){
					return null;
				}
				throw PoolUtil.generateSQLException(e.getMessage(), e);
			}
		}
		
		return result;
	}
	
	/** Closes off all connections in all partitions. */
	public void terminateAllConnections(){
		this.terminationLock.lock();
		try{
			// close off all connections.
			for (int i=0; i < this.pool.partitionCount; i++) {
				this.pool.partitions[i].setUnableToCreateMoreTransactions(false); // we can create new ones now, this is an optimization
				List<ConnectionHandle> clist = new LinkedList<ConnectionHandle>(); 
				this.pool.partitions[i].getFreeConnections().drainTo(clist);
				for (ConnectionHandle c: clist){
					this.pool.destroyConnection(c);
				}

			}
		} finally {
			this.terminationLock.unlock();
		}
	}

}
