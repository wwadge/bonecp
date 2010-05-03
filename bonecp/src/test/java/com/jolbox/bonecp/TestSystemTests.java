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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.jolbox.bonecp.hooks.CoverageHook;
import com.jolbox.bonecp.hooks.CustomHook;



@SuppressWarnings("all") 
public class TestSystemTests {

	private static BoneCPConfig config;
	private static MockJDBCDriver driver;

	@BeforeClass
	public static void setup() throws ClassNotFoundException, SQLException{
		driver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			public Connection answer() throws SQLException {
				return new MockConnection();
			}
		});
		config = CommonTestUtils.getConfigClone();
	}

	@AfterClass
	public static void teardown() throws SQLException{
		driver.disable();
	}
	
	@Before
	public void beforeTest(){
		config.setJdbcUrl(CommonTestUtils.url);
		config.setUsername(CommonTestUtils.username);
		config.setPassword(CommonTestUtils.password);
		config.setIdleConnectionTestPeriod(10000);
		config.setIdleMaxAge(0);
		config.setStatementsCacheSize(0);
		config.setReleaseHelperThreads(0);
		config.setStatementsCachedPerConnection(30);
	}


		
	/** Mostly for code coverage. 
	 * @throws IOException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws ClassNotFoundException */
	@Test
	public void testDataSource() throws SQLException, IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		config.setAcquireIncrement(5);
		config.setMinConnectionsPerPartition(30);
		config.setMaxConnectionsPerPartition(100);
		config.setPartitionCount(1);
		
		
		BoneCPDataSource dsb = new BoneCPDataSource(config);
		dsb.setPartitionCount(1); 
		dsb.setAcquireRetryDelay(-1);
		dsb.setAcquireRetryAttempts(0);
		dsb.setMaxConnectionsPerPartition(100);
		dsb.setMinConnectionsPerPartition(30);
		dsb.setTransactionRecoveryEnabled(true);
		dsb.setConnectionHook(new CoverageHook());
		dsb.setLazyInit(false);
		dsb.setStatementsCachedPerConnection(30);
		dsb.setStatementsCacheSize(30);
		dsb.setReleaseHelperThreads(0);
		dsb.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");
		dsb.isWrapperFor(String.class);
		dsb.setIdleMaxAge(0L);
		dsb.setAcquireIncrement(5);
		dsb.setIdleConnectionTestPeriod(0L);
		dsb.setConnectionTestStatement("test");
		dsb.setInitSQL(CommonTestUtils.TEST_QUERY);
		dsb.setCloseConnectionWatch(true);
		dsb.setLogStatementsEnabled(false);
		dsb.getConnection().close();
		assertNotNull(dsb.getConfig());
		assertNotNull(dsb.toString());
		dsb.setConnectionHookClassName("bad class name");
		assertEquals("bad class name", dsb.getConnectionHookClassName());
		assertNull(dsb.getConnectionHook());
		
		dsb.setConnectionHookClassName("com.jolbox.bonecp.hooks.CustomHook");
		assertTrue(dsb.getConnectionHook() instanceof CustomHook);
		
		File tmp = File.createTempFile("bonecp", "");
		dsb.setLogWriter(new PrintWriter(tmp));
		assertNotNull(dsb.getLogWriter());
		try {
			dsb.setLoginTimeout(0);
			fail("Should throw exception");
		} catch (UnsupportedOperationException e) {
			// do nothing
		}
		
		try {
			dsb.getLoginTimeout();
			fail("Should throw exception");
		} catch (UnsupportedOperationException e) {
			// do nothing
		}
		try {
			dsb.getConnection("test", "test");
			fail("Should throw exception");
		} catch (UnsupportedOperationException e) {
			// do nothing
		}

		
		BoneCPDataSource dsb2 = new BoneCPDataSource(); // empty constructor test
		dsb2.setDriverClass("inexistent");
		try{
			dsb2.getConnection();
			fail("Should fail");
		} catch (SQLException e){
			// do nothing
		}
		
		
		assertNull(dsb.unwrap(String.class));
		assertEquals("com.jolbox.bonecp.MockJDBCDriver", dsb.getDriverClass());
		dsb.setClassLoader(getClass().getClassLoader());
		dsb.loadClass("java.lang.String");
		assertEquals(getClass().getClassLoader(), dsb.getClassLoader());
	}
	
	@Test(expected=SQLException.class)
	public void testDBConnectionInvalidJDBCurl() throws SQLException{
		CommonTestUtils.logTestInfo("Test trying to start up with an invalid URL.");
		config.setJdbcUrl("invalid JDBC URL");
		new BoneCP(config);
		CommonTestUtils.logPass();
	}


	@Test
	public void testGetReleaseSingleThread() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test simple get/release connection from 1 partition");

		config.setMinConnectionsPerPartition(30);
		config.setMaxConnectionsPerPartition(100);
		config.setAcquireIncrement(5);
		config.setPartitionCount(1);
		BoneCP dsb = new BoneCP(config);


		for (int i=0; i<60; i++){
			Connection conn = dsb.getConnection();
			conn.close();
		}
		assertEquals(0, dsb.getTotalLeased());
		assertEquals(30, dsb.getTotalFree());


		dsb.shutdown();
		CommonTestUtils.logPass();
	}


	/** Test that requesting connections from a partition that is empty will fetch it from other partitions that still have connections. */
	@Test
	public void testPartitionDrain() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test connections obtained from alternate partition");

		config.setAcquireIncrement(1);
		config.setMinConnectionsPerPartition(10);
		config.setMaxConnectionsPerPartition(10);
		config.setPartitionCount(2);
		BoneCP dsb = new BoneCP(config);
		for (int i=0; i < 20; i++){
			dsb.getConnection();
		}
		assertEquals(20, dsb.getTotalLeased());
		assertEquals(0, dsb.getTotalFree());
		dsb.close();
		CommonTestUtils.logPass();
	}

	@Test
	public void testMultithreadSinglePartition() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test multiple threads hitting a single partition concurrently");
		config.setAcquireIncrement(5);
		config.setMinConnectionsPerPartition(30);
		config.setMaxConnectionsPerPartition(100);
		config.setPartitionCount(1);

		BoneCPDataSource dsb = new BoneCPDataSource(config);
		dsb.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");

		CommonTestUtils.startThreadTest(100, 100, dsb, 0, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	@Test
	public void testMultithreadMultiPartition() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test multiple threads hitting a multiple partitions concurrently");
		config.setAcquireIncrement(5);
		config.setMinConnectionsPerPartition(10);
		config.setMaxConnectionsPerPartition(25);
		config.setPartitionCount(5);
		config.setReleaseHelperThreads(0);
		BoneCPDataSource dsb = new BoneCPDataSource(config);
		dsb.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");
		CommonTestUtils.startThreadTest(100, 1000, dsb, 0, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	@Test
	public void testMultithreadMultiPartitionWithConstantWorkDelay() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test multiple threads hitting a partition and doing some work on each connection");
		config.setAcquireIncrement(1);
		config.setMinConnectionsPerPartition(10);
		config.setMaxConnectionsPerPartition(10);
		config.setPartitionCount(1);

		BoneCPDataSource dsb = new BoneCPDataSource(config);
		dsb.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");

		CommonTestUtils.startThreadTest(15, 10, dsb, 50, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	@Test
	public void testMultithreadMultiPartitionWithRandomWorkDelay() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test multiple threads hitting a partition and doing some work of random duration on each connection");
		config.setAcquireIncrement(5);
		config.setMinConnectionsPerPartition(10);
		config.setMaxConnectionsPerPartition(25);
		config.setPartitionCount(5);

		BoneCPDataSource dsb = new BoneCPDataSource(config);
		dsb.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");

		CommonTestUtils.startThreadTest(100, 10, dsb, -50, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	/** Tests that new connections are created on the fly. */
	@Test
	public void testConnectionCreate() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Tests that new connections are created on the fly");
		config.setMinConnectionsPerPartition(10);
		config.setMaxConnectionsPerPartition(20);
		config.setAcquireIncrement(5);
		config.setPartitionCount(1);
		config.setReleaseHelperThreads(0);
		config.setPoolAvailabilityThreshold(0);

		BoneCP dsb = new BoneCP(config);

		assertEquals(10, dsb.getTotalCreatedConnections());
		assertEquals(0, dsb.getTotalLeased());

		for (int i=0; i < 10; i++){
			dsb.getConnection();
		}
		assertEquals(10, dsb.getTotalLeased());

		for (int i=0; i < 60; i++) {
			Thread.yield();
			Thread.sleep(500); // give time for pool watch thread to fire up
			if (dsb.getTotalCreatedConnections() == 15) {
				break;
			}
		}
		assertEquals(15, dsb.getTotalCreatedConnections());
		assertEquals(10, dsb.getTotalLeased());
		assertEquals(5, dsb.getTotalFree());

		dsb.shutdown();
		CommonTestUtils.logPass();
	}

	@Test
	public void testClosedConnection() throws InterruptedException, SQLException{
		BoneCP dsb = null ;
		CommonTestUtils.logTestInfo("Tests that closed connections trigger exceptions if use is attempted.");
		config.setMinConnectionsPerPartition(10);
		config.setMaxConnectionsPerPartition(20);
		config.setAcquireIncrement(5);
		config.setPartitionCount(1);
		try{
			dsb = new BoneCP(config);
			Connection conn = dsb.getConnection();
			conn.prepareCall(CommonTestUtils.TEST_QUERY);
			conn.close();
			try{
				conn.prepareCall(CommonTestUtils.TEST_QUERY);
				fail("Should have thrown an exception");
			} catch (SQLException e){
				CommonTestUtils.logPass();
			}
		} finally{
			dsb.shutdown();
		}
	}

	


}
