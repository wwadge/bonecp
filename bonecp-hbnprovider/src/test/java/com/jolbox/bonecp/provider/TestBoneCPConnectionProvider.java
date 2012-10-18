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
package com.jolbox.bonecp.provider;

import static org.easymock.EasyMock.*;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_DRIVER_CLASS;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_PASSWORD;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_URL;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_USERNAME;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.MockConnection;
import com.jolbox.bonecp.MockJDBCAnswer;
import com.jolbox.bonecp.MockJDBCDriver;

/** Test case for the Hibernate boneCP connection provider.
 * @author wallacew
 *
 */
public class TestBoneCPConnectionProvider {
	/** Mock handle. */
	private static BoneCP mockPool;
	/** Mock handle. */
	private static ConnectionHandle mockConnection;
	/** Mock handle. */
	private static Properties mockProperties;
	/** Class under test. */
	private static BoneCPConnectionProvider testClass;
	/** hsqldb driver. */
	private static String URL = "jdbc:mock";
	/** hsqldb username. */
	private static String USERNAME = "sa";
	/** hsqldb password. */
	private static String PASSWORD = "";
	/** hsqldb driver. */
	private static String DRIVER = "com.jolbox.bonecp.MockJDBCDriver";
	/** A dummy query for HSQLDB. */
	public static final String TEST_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
	/** Mock driver handler. */
	private MockJDBCDriver driver;
	
	/** Class setup.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException 
	 */
	@BeforeClass
	public static void setup() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		mockPool = createNiceMock(BoneCP.class);
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockProperties = createNiceMock(Properties.class);
	
	}


	/**
	 * Mock reset.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws SQLException 
	 */
	@Before
	public void before() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException, SQLException{
		testClass = new BoneCPConnectionProvider();

		// wire in our mock
		Field field = testClass.getClass().getDeclaredField("pool");
		field.setAccessible(true);
		field.set(testClass, mockPool);
		
		
		reset(mockPool, mockConnection, mockProperties);
		
		// load the driver.
		this.driver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			public Connection answer() throws SQLException {
				return new MockConnection();
			}
		});
	}

	/**
	 * Cleanup
	 * @throws SQLException
	 */
	@After
	public void deregisterDriver() throws SQLException{
		if (this.driver != null){
			this.driver.unregister();
		}
	}
	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#close()}.
	 */
	@Test
	public void testClose() {
		mockPool.shutdown();
		expectLastCall().once();
		replay(mockPool);
		testClass.close();
		verify(mockPool);
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#closeConnection(java.sql.Connection)}.
	 * @throws SQLException 
	 */
	@Test
	public void testCloseConnection() throws SQLException {
		mockConnection.close();
		expectLastCall().once();
		replay(mockPool);
		testClass.closeConnection(mockConnection);
		verify(mockPool);
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#configure(java.util.Properties)}.
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	@Test
	public void testConfigure() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, SQLException {
		
		Class.forName("com.jolbox.bonecp.MockJDBCDriver");
		expect(mockProperties.getProperty("bonecp.statementsCacheSize")).andReturn("40").anyTimes();
		expect(mockProperties.getProperty("bonecp.minConnectionsPerPartition")).andReturn("20").anyTimes();
		expect(mockProperties.getProperty("bonecp.maxConnectionsPerPartition")).andReturn("50").anyTimes(); 
		expect(mockProperties.getProperty("bonecp.acquireIncrement")).andReturn("5").anyTimes();
		expect(mockProperties.getProperty("bonecp.partitionCount")).andReturn("5").anyTimes();
		expect(mockProperties.getProperty("bonecp.releaseHelperThreads")).andReturn("3").anyTimes();
		expect(mockProperties.getProperty("bonecp.idleConnectionTestPeriod")).andReturn("60").anyTimes();
		expect(mockProperties.getProperty("bonecp.idleMaxAge")).andReturn("240").anyTimes();
		expect(mockProperties.getProperty("javax.persistence.jdbc.url")).andReturn(URL).anyTimes();
		expect(mockProperties.getProperty("javax.persistence.jdbc.user")).andReturn(USERNAME).anyTimes();
		expect(mockProperties.getProperty("javax.persistence.jdbc.password")).andReturn(PASSWORD).anyTimes();
		expect(mockProperties.getProperty("javax.persistence.jdbc.driver")).andReturn(DRIVER).anyTimes();
		expect(mockProperties.getProperty("bonecp.connectionHookClassName")).andReturn("com.jolbox.bonecp.provider.CustomHook").anyTimes();
		expect(mockProperties.getProperty("bonecp.connectionTestStatement")).andReturn(TEST_QUERY).anyTimes();
		expect(mockProperties.getProperty("bonecp.initSQL")).andReturn(TEST_QUERY).anyTimes();
		expect(mockProperties.getProperty("bonecp.logStatementsEnabled")).andReturn("true").anyTimes();



		BoneCPConnectionProvider partialTestClass = createMockBuilder(BoneCPConnectionProvider.class).addMockedMethod( 
				BoneCPConnectionProvider.class.getDeclaredMethod("createPool", BoneCPConfig.class)).createMock();
		expect(partialTestClass.createPool((BoneCPConfig)anyObject())).andReturn(mockPool).once();
		
		replay(mockProperties, partialTestClass);
		partialTestClass.configure(mockProperties);

		// fetch the configuration object and check that everything is as we passed
		BoneCPConfig config = partialTestClass.getConfig();

		assertEquals(40, config.getStatementsCacheSize());
//		assertEquals(30, config.getStatementsCachedPerConnection());
		assertEquals(20, config.getMinConnectionsPerPartition());
		assertEquals(50, config.getMaxConnectionsPerPartition());
		assertEquals(5, config.getAcquireIncrement());
		assertEquals(5, config.getPartitionCount());
		assertEquals(0, config.getReleaseHelperThreads());
		assertEquals(60, config.getIdleConnectionTestPeriodInMinutes());
		assertEquals(240, config.getIdleMaxAgeInMinutes()); 
		assertEquals(URL, config.getJdbcUrl());
		assertEquals(USERNAME, config.getUsername());
		assertEquals(PASSWORD, config.getPassword());
		assertEquals(TEST_QUERY, config.getInitSQL());
		assertEquals(true, config.isLogStatementsEnabled());


		verify(mockProperties, partialTestClass);
		reset(mockProperties);
		expect(mockProperties.getProperty(CONFIG_CONNECTION_DRIVER_CLASS)).andReturn(null).anyTimes();
		replay(mockProperties);
		try{
			testClass.configure(mockProperties);
			fail("Should have failed with exception");
		} catch (HibernateException e){ 
			// do nothing
		}

		reset(mockProperties);
		expect(mockProperties.getProperty(CONFIG_CONNECTION_DRIVER_CLASS)).andReturn(DRIVER).anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_URL, "JDBC URL NOT SET IN CONFIG")).andReturn("somethinginvalid").anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_USERNAME, "username not set")).andReturn(USERNAME).anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_PASSWORD, "password not set")).andReturn(PASSWORD).anyTimes();

		replay(mockProperties);
		try{
			testClass.configure(mockProperties);
			fail("Should have failed with exception");
		} catch (HibernateException e){
			// do nothing
		}
		

		verify(mockProperties);
		reset(mockProperties);
		expect(mockProperties.getProperty(CONFIG_CONNECTION_DRIVER_CLASS)).andReturn("somethingbad").anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_URL, "JDBC URL NOT SET IN CONFIG")).andReturn("somethinginvalid").anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_USERNAME, "username not set")).andReturn(USERNAME).anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_PASSWORD, "password not set")).andReturn(PASSWORD).anyTimes();

		replay(mockProperties);
		try{
			testClass.configure(mockProperties);
			fail("Should have failed with exception");
		} catch (HibernateException e){ 
			// do nothing
		}

		testClass.setClassLoader(getClass().getClassLoader());
		testClass.loadClass("java.lang.String");

		testClass.setClassLoader(this.getClass().getClassLoader());
		assertEquals(this.getClass().getClassLoader(), testClass.getClassLoader());
		
		// coverage stuff
		reset(mockProperties);
		expect(mockProperties.getProperty("bonecp.partitionCount")).andReturn("something bad");
		expect(mockProperties.getProperty("bonecp.logStatementsEnabled")).andReturn("something bad");
		expect(mockProperties.getProperty("bonecp.idleMaxAge")).andReturn("something bad");
		replay(mockProperties);
		try{
			testClass.configure(mockProperties);
			fail("Should have failed with exception");
		} catch (HibernateException e){ 
			// do nothing
		}
		
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#createPool(BoneCPConfig)}.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testCreatePool() throws SQLException, ClassNotFoundException {
	
		BoneCPConfig mockConfig = createNiceMock(BoneCPConfig.class);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriodInMinutes()).andReturn(100L).anyTimes();
		expect(mockConfig.getUsername()).andReturn("somethingbad").anyTimes();
		expect(mockConfig.getPassword()).andReturn("somethingbad").anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("invalid").anyTimes();
//		expect(mockConfig.getReleaseHelperThreads()).andReturn(1).once().andReturn(0).anyTimes();
		replay(mockConfig);
		try{
			testClass.createPool(mockConfig);
			fail("Should throw an exception");
		} catch (RuntimeException e){
			// do nothing
		}
		verify(mockConfig);
		
		reset(mockConfig);
		
		Class.forName(DRIVER);
		mockConfig = createNiceMock(BoneCPConfig.class);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriodInMinutes()).andReturn(100L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(USERNAME).anyTimes();
		expect(mockConfig.getPassword()).andReturn(PASSWORD).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn(URL).anyTimes();
		replay(mockConfig);
		
		try{
			testClass.createPool(mockConfig);
		} catch (RuntimeException e){
			fail("Should pass");
		}
		verify(mockConfig);
		
		
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#getConnection()}.
	 * @throws SQLException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testGetConnection() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		Field field = testClass.getClass().getDeclaredField("autocommit");
		field.setAccessible(true);
		field.set(testClass, true);


		field = testClass.getClass().getDeclaredField("isolation");
		field.setAccessible(true);
		field.set(testClass, 8);


		expect(mockPool.getConnection()).andReturn(mockConnection).once();
		expect(mockConnection.getAutoCommit()).andReturn(false).once();
		expect(mockConnection.getTransactionIsolation()).andReturn(0).once();
		mockConnection.setTransactionIsolation(8);
		expectLastCall().once();
		replay(mockPool, mockConnection);
		testClass.getConnection();
		verify(mockPool, mockConnection);

	}
	
	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#getConnection()}.
	 * @throws SQLException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testGetConnectionWithException() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		Field field = testClass.getClass().getDeclaredField("isolation");
		field.setAccessible(true);
		field.set(testClass, 8);


		expect(mockPool.getConnection()).andReturn(mockConnection).once();
		expect(mockConnection.getTransactionIsolation()).andThrow(new SQLException()).once();
		mockConnection.close();
		expectLastCall().once();
		replay(mockPool, mockConnection);
		try{
			testClass.getConnection();
			fail("Should throw an exception");
		} catch(SQLException e){
			// do nothing
		}
		verify(mockPool, mockConnection);

	}

	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#getConnection()}.
	 * @throws SQLException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testGetConnectionWithExceptionEvenInFinally() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		Field field = testClass.getClass().getDeclaredField("isolation");
		field.setAccessible(true);
		field.set(testClass, 8);


		expect(mockPool.getConnection()).andReturn(mockConnection).once();
		expect(mockConnection.getTransactionIsolation()).andThrow(new SQLException()).once();
		mockConnection.close();
		expectLastCall().andThrow(new SQLException()).once();
		replay(mockPool, mockConnection);
		try{
			testClass.getConnection();
			fail("Should throw an exception");
		} catch(SQLException e){
			// do nothing
		}
		verify(mockPool, mockConnection);

	}
	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#supportsAggressiveRelease()}.
	 */
	@Test
	public void testSupportsAggressiveRelease() {
		assertFalse(testClass.supportsAggressiveRelease());
	}

}
