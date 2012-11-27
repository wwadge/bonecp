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
/**
 * 
 */
package com.jolbox.bonecp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;

/**
 * @author wwadge
 *
 */
public class TestCachedConnectionStrategy {

	private MockJDBCDriver driver;
	private BoneCP poolClass;

	BoneCPConfig config = new BoneCPConfig();

	@Before
	public void setup() throws SQLException{


		driver = new MockJDBCDriver(new MockJDBCAnswer() {

			public Connection answer() throws SQLException {
				return new MockConnection();
			}
		});

		config.setPoolStrategy("CACHED");
		config.setPartitionCount(1);
		config.setMaxConnectionsPerPartition(5);
		config.setMinConnectionsPerPartition(5);
		config.setJdbcUrl("jdbc:mock");


	}

	@After
	public void tearDown() throws SQLException{
		//	poolClass.close();
		driver.unregister();
	}

	@Test
	public void testNormalCase() throws SQLException {
		poolClass = new BoneCP(config);
		ConnectionHandle c = (ConnectionHandle) poolClass.getConnection();
		Connection handle = c.getInternalConnection();
		c.close();

		// getting it again in this thread should give us the exact same object
		ConnectionHandle c2 = (ConnectionHandle) poolClass.getConnection();
		assertEquals(handle, c2.getInternalConnection());
		c2.close();

		poolClass.close();
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testOneThreadTwoConnections() throws SQLException{	
		poolClass = new BoneCP(config);
		ConnectionHandle c1 = (ConnectionHandle) poolClass.getConnection();
		c1.setDebugHandle(123);
		assertFalse(c1.logicallyClosed.get());

		ConnectionHandle c2 = (ConnectionHandle) poolClass.getConnection();
		c2.setDebugHandle(456);
		assertFalse(c2.logicallyClosed.get());
		assertNotSame(c1.getDebugHandle(), c2.getDebugHandle());
		assertNotSame(c1.getInternalConnection(), c2.getInternalConnection());

		Connection c1Conn = c1.getInternalConnection();

		// closing off a connection should result in:
		// that connectionHandle having a blank internal
		// the old internal = in TL
		// the TL contains a new CH
		c1.close();
		SimpleEntry<ConnectionHandle, Boolean> handle = ((CachedConnectionStrategyThreadLocal)(((CachedConnectionStrategy)c1.getPool().connectionStrategy).tlConnections)).dumbGet();
		assertNotSame(c1.getDebugHandle(), handle.getKey().getDebugHandle()); // still here?
		assertNotSame(c1.getInternalConnection(), handle.getKey().getInternalConnection());
		assertNull(c1.getInternalConnection());
		assertEquals(c1Conn,handle.getKey().getInternalConnection());

		assertEquals(3, poolClass.partitions[0].getFreeConnections().size());
		c2.close();
		assertEquals(4, poolClass.partitions[0].getFreeConnections().size());


		poolClass.close();

	}

	@Test 
	public void testMoreThreadsThanConnections() throws SQLException, InterruptedException, CloneNotSupportedException{
		BoneCPConfig config = this.config.clone();
		config.setNullOnConnectionTimeout(true);
		config.setConnectionTimeoutInMs(10);
		poolClass = new BoneCP(config);
		final CountDownLatch cdl = new CountDownLatch(5);
		final CountDownLatch cdlTerminate = new CountDownLatch(1);
		final CountDownLatch crudeJoin = new CountDownLatch(5);
		for (int i=0; i < 5; i++){
			new Thread(){


				public void run() {
					try {
						Connection c = poolClass.getConnection();
						cdl.countDown();
						cdlTerminate.await(); // everyone waits
						c.close();
						crudeJoin.countDown();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		cdl.await();

		// this next request should make the pool switch over (permanently) to the fallback strategy
		Connection c = poolClass.getConnection();
		assertNull(c); // we've run out
		cdlTerminate.countDown(); // release the threads holding the lock
		// try again
		c = poolClass.getConnection();
		assertNotNull(c); // we can get new connections again

		// by the time 1 thread times out, we'd already have flipped. Check to make sure
		assertFalse(poolClass.cachedPoolStrategy);
		c.close();
		crudeJoin.await(); // wait till everyone said close
		assertEquals(5, poolClass.partitions[0].getFreeConnections().size());

		poolClass.close();

	}

	/** Same test as above but this time the threads are done from using it. All connections should go back to the normal queue in the fallback
	 * strategy.
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws CloneNotSupportedException 
	 */
	@Test 
	public void testMoreThreadsThanConnectionsSteal() throws SQLException, InterruptedException, CloneNotSupportedException{
		BoneCPConfig config = this.config.clone();

		config.setNullOnConnectionTimeout(false);
		config.setConnectionTimeoutInMs(Long.MAX_VALUE);

		poolClass = new BoneCP(config);
		final CountDownLatch cdl = new CountDownLatch(5);
		for (int i=0; i < 5; i++){
			new Thread(){

				public void run() {
					try {
						Connection c = poolClass.getConnection();
						c.close();
						cdl.countDown();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		cdl.await();

		// this next request should make the pool switch over (permanently) to the fallback strategy
		poolClass.getConnection().close();
		// by the time 1 thread times out, we'd already have flipped. Check to make sure
		assertFalse(poolClass.cachedPoolStrategy);
		assertEquals(5, poolClass.partitions[0].getFreeConnections().size());
	}
	 
	@Test
	@Ignore
	public void testThreadDies() throws CloneNotSupportedException, SQLException, InterruptedException{
		BoneCPConfig config = this.config.clone();


		poolClass = new BoneCP(config);
		
		Thread t = new Thread(){

			public void run() {
				try {
					 poolClass.getConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		
		t.start();
		t.join();
		System.gc();
		System.gc();
		System.gc();
		assertEquals(5, poolClass.partitions[0].getFreeConnections().size());
		
	}
	
	@Test
	public void testCoverage() throws SQLException{
		poolClass = new BoneCP(config);
		try{
			poolClass.connectionStrategy.pollConnection();
			Assert.fail("Should throw an exception");
		} catch(Exception e){
			// nothing
		}
		
		BoneCP mockPool = EasyMock.createNiceMock(BoneCP.class);
		CachedConnectionStrategy ccs = new CachedConnectionStrategy(mockPool , new DefaultConnectionStrategy(mockPool));
		ConnectionHandle mockConnection = EasyMock.createNiceMock(ConnectionHandle.class);
		mockConnection.logicallyClosed = new AtomicBoolean(true);
		ccs.threadFinalizableRefs.put(mockConnection, new FinalizableWeakReference<Thread>(Thread.currentThread(), new FinalizableReferenceQueue()) {
			public void finalizeReferent() {
		}
	});
		
		mockPool.releaseConnection((Connection)EasyMock.anyObject());
		EasyMock.expectLastCall().andThrow(new SQLException("foo")).once();
		EasyMock.replay(mockPool);
		ccs.stealExistingAllocations();
		
	}
}
