/**
 * Copyright 2009 Wallace Wadge
 *
 * This file is part of BoneCP.
 *
 * BoneCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BoneCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package com.jolbox.bonecp.provider;

import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_ACQUIRE_INCREMENT;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_DRIVER_CLASS;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_HOOK_CLASS;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_PASSWORD;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_URL;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_CONNECTION_USERNAME;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_IDLE_CONNECTION_TEST_PERIOD;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_IDLE_MAX_AGE;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_INIT_SQL;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_MAX_CONNECTIONS_PER_PARTITION;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_MIN_CONNECTIONS_PER_PARTITION;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_PARTITION_COUNT;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_PREPARED_STATEMENT_CACHE_SIZE;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_RELEASE_HELPER_THREADS;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_STATEMENTS_CACHED_PER_CONNECTION;
import static com.jolbox.bonecp.provider.BoneCPConnectionProvider.CONFIG_TEST_STATEMENT;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.ConnectionHandle;

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
	private static String URL = "jdbc:hsqldb:mem:test";
	/** hsqldb username. */
	private static String USERNAME = "sa";
	/** hsqldb password. */
	private static String PASSWORD = "";
	/** hsqldb driver. */
	private static String DRIVER = "org.hsqldb.jdbcDriver";
	/** A dummy query for HSQLDB. */
	public static final String TEST_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
	
	/** Class setup.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@BeforeClass
	public static void setup() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
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
	 */
	@Before
	public void before() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException{
		testClass = new BoneCPConnectionProvider();

		// wire in our mock
		Field field = testClass.getClass().getDeclaredField("pool");
		field.setAccessible(true);
		field.set(testClass, mockPool);
		
		reset(mockPool, mockConnection, mockProperties);
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
	 */
	@Test
	public void testConfigure() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException {
		expect(mockProperties.getProperty(CONFIG_TEST_STATEMENT)).andReturn(null).anyTimes();
		expect(mockProperties.getProperty(CONFIG_PREPARED_STATEMENT_CACHE_SIZE)).andReturn("40").anyTimes();
		expect(mockProperties.getProperty(CONFIG_STATEMENTS_CACHED_PER_CONNECTION)).andReturn("30").anyTimes();
		expect(mockProperties.getProperty(CONFIG_MIN_CONNECTIONS_PER_PARTITION)).andReturn("20").anyTimes();
		expect(mockProperties.getProperty(CONFIG_MAX_CONNECTIONS_PER_PARTITION)).andReturn("50").anyTimes(); 
		expect(mockProperties.getProperty(CONFIG_ACQUIRE_INCREMENT)).andReturn("5").anyTimes();
		expect(mockProperties.getProperty(CONFIG_PARTITION_COUNT)).andReturn("5").anyTimes();
		expect(mockProperties.getProperty(CONFIG_RELEASE_HELPER_THREADS)).andReturn("3").anyTimes();
		expect(mockProperties.getProperty(CONFIG_IDLE_CONNECTION_TEST_PERIOD)).andReturn("60").anyTimes();
		expect(mockProperties.getProperty(CONFIG_IDLE_MAX_AGE)).andReturn("240").anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_URL, "JDBC URL NOT SET IN CONFIG")).andReturn(URL).anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_USERNAME, "username not set")).andReturn(USERNAME).anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_PASSWORD, "password not set")).andReturn(PASSWORD).anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_DRIVER_CLASS)).andReturn(DRIVER).anyTimes();
		expect(mockProperties.getProperty(CONFIG_CONNECTION_HOOK_CLASS)).andReturn("com.jolbox.bonecp.provider.CustomHook").anyTimes();
		expect(mockProperties.getProperty(CONFIG_INIT_SQL)).andReturn(TEST_QUERY).anyTimes();


		BoneCPConnectionProvider partialTestClass = createNiceMock(BoneCPConnectionProvider.class, 
				BoneCPConnectionProvider.class.getDeclaredMethod("createPool", BoneCPConfig.class));
		expect(partialTestClass.createPool((BoneCPConfig)anyObject())).andReturn(mockPool).once();
		
		replay(mockProperties, partialTestClass);
		partialTestClass.configure(mockProperties);

		// fetch the configuration object and check that everything is as we passed
		BoneCPConfig config = partialTestClass.getConfig();

		assertEquals(40, config.getPreparedStatementsCacheSize());
		assertEquals(30, config.getStatementsCachedPerConnection());
		assertEquals(20, config.getMinConnectionsPerPartition());
		assertEquals(50, config.getMaxConnectionsPerPartition());
		assertEquals(5, config.getAcquireIncrement());
		assertEquals(5, config.getPartitionCount());
		assertEquals(3, config.getReleaseHelperThreads());
		assertEquals(60, config.getIdleConnectionTestPeriod());
		assertEquals(240, config.getIdleMaxAge()); 
		assertEquals(URL, config.getJdbcUrl());
		assertEquals(USERNAME, config.getUsername());
		assertEquals(PASSWORD, config.getPassword());
		assertEquals(TEST_QUERY, config.getInitSQL());


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
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn("somethingbad").anyTimes();
		expect(mockConfig.getPassword()).andReturn("somethingbad").anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("somethingbad").anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(1).once().andReturn(0).anyTimes();
		replay(mockConfig);
		try{
			testClass.createPool(mockConfig);
			fail("Should throw an exception");
		} catch (RuntimeException e){
			// do nothing
		}
		verify(mockConfig);
		
		reset(mockConfig);
		
		Class.forName("org.hsqldb.jdbcDriver");
		mockConfig = createNiceMock(BoneCPConfig.class);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(USERNAME).anyTimes();
		expect(mockConfig.getPassword()).andReturn(PASSWORD).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn(URL).anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(1).once().andReturn(0).anyTimes();
		replay(mockConfig);
		
		try{
			testClass.createPool(mockConfig);
		} catch (RuntimeException e){
			fail("Should pass");
		}
		verify(mockConfig);
		
		
	}
	/**
	 * Test method for {@link com.jolbox.bonecp.provider.BoneCPConnectionProvider#configure(java.util.Properties)}.
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testConfigParseNumber() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		expect(mockProperties.getProperty("test")).andReturn("99");
		replay(mockProperties);
		Method method = testClass.getClass().getDeclaredMethod("configParseNumber", new Class[]{Properties.class, String.class, int.class});
		method.setAccessible(true);

		int result = (Integer) method.invoke(testClass, mockProperties, "test", 123);
		assertEquals(99, result);
		verify(mockProperties);

		reset(mockProperties);
		expect(mockProperties.getProperty("test")).andReturn("somethingbad");
		replay(mockProperties);

		result = (Integer) method.invoke(testClass, mockProperties, "test", 123);
		assertEquals(123, result);
		verify(mockProperties);

		// test not finding a value
		reset(mockProperties);
		expect(mockProperties.getProperty("test")).andReturn(null);
		replay(mockProperties);

		result = (Integer) method.invoke(testClass, mockProperties, "test", 123);
		assertEquals(123, result);
		verify(mockProperties);


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
		field.set(testClass, 0);


		expect(mockPool.getConnection()).andReturn(mockConnection).once();
		expect(mockConnection.getAutoCommit()).andReturn(false).once();
		mockConnection.setTransactionIsolation(0);
		expectLastCall().once();
		replay(mockPool, mockConnection);
		testClass.getConnection();
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
