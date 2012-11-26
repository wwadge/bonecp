/**
 * 
 */
package com.jolbox.bonecp;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author wwadge
 *
 */
@Ignore
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
		c.setDebugHandle(123);
		c.close();

		// getting it again in this thread should give us the exact same object
		ConnectionHandle c2 = (ConnectionHandle) poolClass.getConnection();
		assertEquals(123, c2.getDebugHandle());
		c2.close();
		
		poolClass.close();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testOneThreadTwoConnections() throws SQLException{	
		poolClass = new BoneCP(config);
		ConnectionHandle c1 = (ConnectionHandle) poolClass.getConnection();
		c1.setDebugHandle(123);
		assertTrue(c1.inUseInThreadLocalContext.get());

		ConnectionHandle c2 = (ConnectionHandle) poolClass.getConnection();
		c2.setDebugHandle(456);
		assertFalse(c2.inUseInThreadLocalContext.get());
		assertNotSame(c1.getDebugHandle(), c2.getDebugHandle());

		ConnectionHandle handle = ((CachedConnectionStrategyThreadLocal)(((CachedConnectionStrategy)c1.getPool().connectionStrategy).tlConnections)).dumbGet();
		assertEquals(c1.getDebugHandle(), handle.getDebugHandle());
		assertTrue(handle.inUseInThreadLocalContext.get()); // connection marked as thread-bound and in use

		c1.close();
		handle = ((CachedConnectionStrategyThreadLocal)(((CachedConnectionStrategy)c1.getPool().connectionStrategy).tlConnections)).dumbGet();
		assertEquals(c1.getDebugHandle(), handle.getDebugHandle()); // still here?
		assertFalse(handle.inUseInThreadLocalContext.get()); // connection marked as not in use


		c2.close();
		handle = ((CachedConnectionStrategyThreadLocal)(((CachedConnectionStrategy)c1.getPool().connectionStrategy).tlConnections)).dumbGet();
		assertNotSame(c2.getDebugHandle(), handle.getDebugHandle()); // c2 shouldn't have touched anything
		poolClass.close();

	}

	@Test 
	@Ignore
	public void testMoreThreadsThanConnections() throws SQLException, InterruptedException, CloneNotSupportedException{
		BoneCPConfig config = this.config.clone();
		config.setNullOnConnectionTimeout(true);
		config.setConnectionTimeoutInMs(500);
		poolClass = new BoneCP(config);
		final CountDownLatch cdl = new CountDownLatch(5);
		final CountDownLatch cdlTerminate = new CountDownLatch(1);
		for (int i=0; i < 5; i++){
			new Thread(){

				public void run() {
					try {
						Connection c = poolClass.getConnection();
						cdl.countDown();
						cdlTerminate.await(); // everyone waits
						c.close();
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
						cdl.countDown();
						c.close();
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
public void testCoverage() throws SQLException{
	poolClass = new BoneCP(config);
	try{
		poolClass.connectionStrategy.pollConnection();
		Assert.fail("Should throw an exception");
	} catch(Exception e){
		// nothing
	}
}
}
