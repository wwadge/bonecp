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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.hooks.CustomHook;



@SuppressWarnings("all") 
public class TestSystemTests {

	@BeforeClass
	public static void setup() throws ClassNotFoundException{
		Class.forName("org.hsqldb.jdbcDriver");
	}

	@Before
	public void beforeTest(){
		CommonTestUtils.config.setJdbcUrl(CommonTestUtils.url);
		CommonTestUtils.config.setUsername(CommonTestUtils.username);
		CommonTestUtils.config.setPassword(CommonTestUtils.password);
		CommonTestUtils.config.setIdleConnectionTestPeriod(10000);
		CommonTestUtils.config.setIdleMaxAge(0);
		CommonTestUtils.config.setStatementsCacheSize(0);
		CommonTestUtils.config.setReleaseHelperThreads(0);
		CommonTestUtils.config.setStatementsCachedPerConnection(30);
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
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setMinConnectionsPerPartition(30);
		CommonTestUtils.config.setMaxConnectionsPerPartition(100);
		CommonTestUtils.config.setPartitionCount(1);
		
		
		BoneCPDataSource dsb = new BoneCPDataSource(CommonTestUtils.config);
		dsb.setPreparedStatementCacheSize(0);
		dsb.setPartitionCount(1);
		dsb.setPartitionCount("1");
		dsb.setPreparedStatementCacheSize("0");
		dsb.setMaxConnectionsPerPartition(100);
		dsb.setMaxConnectionsPerPartition("100");
		dsb.setMinConnectionsPerPartition(30);
		dsb.setMinConnectionsPerPartition("30");
		dsb.setStatementsCachedPerConnection("30");
		dsb.setStatementsCachedPerConnection(30);
		dsb.setReleaseHelperThreads(0);
		dsb.setReleaseHelperThreads("0");
		dsb.setDriverClass("org.hsqldb.jdbcDriver");
		dsb.isWrapperFor(String.class);
		dsb.setIdleMaxAge(0L);
		dsb.setIdleMaxAge("0");
		dsb.setAcquireIncrement(5);
		dsb.setAcquireIncrement("5");
		dsb.setIdleConnectionTestPeriod(0L);
		dsb.setIdleConnectionTestPeriod("0");
		dsb.setConnectionTestStatement("test");
		dsb.setInitSQL(CommonTestUtils.TEST_QUERY);
		dsb.setCloseConnectionWatch(true);
		dsb.setLogStatementsEnabled(false);
		dsb.setAcquireRetryDelay(1000);
		dsb.setAcquireRetryDelay("1000");
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
		
		Method method = dsb.getClass().getDeclaredMethod("parseNumber", new Class[]{String.class, int.class});
		method.setAccessible(true);
		int result = (Integer) method.invoke(dsb, "badNumber", 123);
		// should fallback to default number
		assertEquals(123, result);
		
		assertNull(dsb.unwrap(String.class));
		assertEquals("5", dsb.getAcquireIncrement());
		assertEquals("30", dsb.getMinConnectionsPerPartition());
		assertEquals("100", dsb.getMaxConnectionsPerPartition());
		assertEquals("1", dsb.getPartitions());
		assertEquals("0", dsb.getIdleConnectionTestPeriod());
		assertEquals("0", dsb.getIdleMaxAge());
		assertEquals(CommonTestUtils.url, dsb.getJdbcUrl());
		assertEquals(CommonTestUtils.username, dsb.getUsername());
		assertEquals(CommonTestUtils.password, dsb.getPassword());
		assertEquals("0", dsb.getPreparedStatementCacheSize());
		assertEquals("0", dsb.getReleaseHelperThreads());
		assertEquals("30", dsb.getStatementsCachedPerConnection());
		assertEquals("test", dsb.getConnectionTestStatement());
		assertEquals("org.hsqldb.jdbcDriver", dsb.getDriverClass());
		assertEquals(CommonTestUtils.TEST_QUERY, dsb.getInitSQL());
		assertEquals(true, dsb.isCloseConnectionWatch());
		assertEquals(false, dsb.isLogStatementsEnabled());
		assertEquals("1000", dsb.getAcquireRetryDelay());
		dsb.setClassLoader(getClass().getClassLoader());
		dsb.loadClass("java.lang.String");
		dsb.setLazyInit(true);
		assertEquals(true, dsb.isLazyInit());
		dsb.setLazyInit(false);
		assertEquals(getClass().getClassLoader(), dsb.getClassLoader());

		method = dsb.getClass().getDeclaredMethod("safePrint", new Class[]{String.class});
		method.setAccessible(true);
		assertTrue("".equals(method.invoke(dsb, new Object[]{null})));
		String tmpString = "foo";
		assertEquals(tmpString, method.invoke(dsb, tmpString));

	}
	
	@Test(expected=SQLException.class)
	public void testDBConnectionInvalidJDBCurl() throws SQLException{
		CommonTestUtils.logTestInfo("Test trying to start up with an invalid URL.");
		CommonTestUtils.config.setJdbcUrl("invalid JDBC URL");
		new BoneCP(CommonTestUtils.config);
		CommonTestUtils.logPass();
	}

	@Test(expected=SQLException.class)
	public void testDBConnectionInvalidUsername() throws SQLException{
		CommonTestUtils.logTestInfo("Test trying to start up with an invalid username/pass combo.");
		CommonTestUtils.config.setUsername("non existent");
		new BoneCP(CommonTestUtils.config);
		CommonTestUtils.logPass();
	}


	@Test
	public void testConnectionGivenButDBLost() throws SQLException{
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setMinConnectionsPerPartition(30);
		CommonTestUtils.config.setMaxConnectionsPerPartition(100);
		CommonTestUtils.config.setPartitionCount(1);
		BoneCP dsb = new BoneCP(CommonTestUtils.config);
		Connection con = dsb.getConnection();
		// kill off the db...
		String sql = "SHUTDOWN"; // hsqldb interprets this as a request to terminate
		Statement stmt = con.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();

		stmt = con.createStatement();
		try{
			stmt.execute(CommonTestUtils.TEST_QUERY);
			fail("Connection should have been marked as broken");
		} catch (Exception e){
			assertTrue(((ConnectionHandle)con).isPossiblyBroken());
		}

		con.close();

	}


	@Test
	public void testGetReleaseSingleThread() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test simple get/release connection from 1 partition");

		CommonTestUtils.config.setMinConnectionsPerPartition(30);
		CommonTestUtils.config.setMaxConnectionsPerPartition(100);
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setPartitionCount(1);
		BoneCP dsb = new BoneCP(CommonTestUtils.config);


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

		CommonTestUtils.config.setAcquireIncrement(1);
		CommonTestUtils.config.setMinConnectionsPerPartition(10);
		CommonTestUtils.config.setMaxConnectionsPerPartition(10);
		CommonTestUtils.config.setPartitionCount(2);
		BoneCP dsb = new BoneCP(CommonTestUtils.config);
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
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setMinConnectionsPerPartition(30);
		CommonTestUtils.config.setMaxConnectionsPerPartition(100);
		CommonTestUtils.config.setPartitionCount(1);

		BoneCPDataSource dsb = new BoneCPDataSource(CommonTestUtils.config);
		dsb.setDriverClass("org.hsqldb.jdbcDriver");

		CommonTestUtils.startThreadTest(100, 100, dsb, 0, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	@Test
	public void testMultithreadMultiPartition() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test multiple threads hitting a multiple partitions concurrently");
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setMinConnectionsPerPartition(10);
		CommonTestUtils.config.setMaxConnectionsPerPartition(25);
		CommonTestUtils.config.setPartitionCount(5);
		CommonTestUtils.config.setReleaseHelperThreads(0);
		BoneCPDataSource dsb = new BoneCPDataSource(CommonTestUtils.config);
		dsb.setDriverClass("org.hsqldb.jdbcDriver");
		CommonTestUtils.startThreadTest(100, 1000, dsb, 0, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	@Test
	public void testMultithreadMultiPartitionWithConstantWorkDelay() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test multiple threads hitting a partition and doing some work on each connection");
		CommonTestUtils.config.setAcquireIncrement(1);
		CommonTestUtils.config.setMinConnectionsPerPartition(10);
		CommonTestUtils.config.setMaxConnectionsPerPartition(10);
		CommonTestUtils.config.setPartitionCount(1);

		BoneCPDataSource dsb = new BoneCPDataSource(CommonTestUtils.config);
		dsb.setDriverClass("org.hsqldb.jdbcDriver");

		CommonTestUtils.startThreadTest(15, 10, dsb, 50, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	@Test
	public void testMultithreadMultiPartitionWithRandomWorkDelay() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Test multiple threads hitting a partition and doing some work of random duration on each connection");
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setMinConnectionsPerPartition(10);
		CommonTestUtils.config.setMaxConnectionsPerPartition(25);
		CommonTestUtils.config.setPartitionCount(5);

		BoneCPDataSource dsb = new BoneCPDataSource(CommonTestUtils.config);
		dsb.setDriverClass("org.hsqldb.jdbcDriver");

		CommonTestUtils.startThreadTest(100, 10, dsb, -50, false);
		assertEquals(0, dsb.getTotalLeased());
		dsb.close();
		CommonTestUtils.logPass();
	}

	/** Tests that new connections are created on the fly. */
	@Test
	public void testConnectionCreate() throws InterruptedException, SQLException{
		CommonTestUtils.logTestInfo("Tests that new connections are created on the fly");
		CommonTestUtils.config.setMinConnectionsPerPartition(10);
		CommonTestUtils.config.setMaxConnectionsPerPartition(20);
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setPartitionCount(1);
		CommonTestUtils.config.setReleaseHelperThreads(0);

		BoneCP dsb = new BoneCP(CommonTestUtils.config);

		assertEquals(10, dsb.getTotalCreatedConnections());


		for (int i=0; i < 10; i++){
			dsb.getConnection();
		}

		for (int i=0; i < 60; i++) {
			Thread.yield();
			Thread.sleep(1000); // give time for pool watch thread to fire up
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
		CommonTestUtils.config.setMinConnectionsPerPartition(10);
		CommonTestUtils.config.setMaxConnectionsPerPartition(20);
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setPartitionCount(1);
		try{
			dsb = new BoneCP(CommonTestUtils.config);
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

