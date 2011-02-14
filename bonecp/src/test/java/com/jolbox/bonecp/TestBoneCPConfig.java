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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.hooks.AbstractConnectionHook;
import com.jolbox.bonecp.hooks.ConnectionHook;

/** Tests config object.
 * @author wwadge
 *
 */
public class TestBoneCPConfig {
	/** Config handle. */
	static BoneCPConfig config;
	
	/** Stub out any calls to logger.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws CloneNotSupportedException 
	 */
	@BeforeClass
	public static void setup() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, CloneNotSupportedException{
		config = CommonTestUtils.getConfigClone();
	}

	/** Tests configs using xml setups.
	 * @throws Exception
	 */
	@Test
	public void testXMLConfig() throws Exception{
		// read off from the default bonecp-config.xml
//		System.out
//				.println(BoneCPConfig.class.getResource("/bonecp-config.xml"));
		BoneCPConfig config = new BoneCPConfig("specialApp");
		assertEquals(99, config.getMinConnectionsPerPartition());
	}
	
	/** Tests configs using xml setups.
	 * @throws Exception
	 */
	@Test
	public void testXMLConfig2() throws Exception{
		// read off from the default bonecp-config.xml
		BoneCPConfig config = new BoneCPConfig("specialApp2");
		assertEquals(123, config.getMinConnectionsPerPartition());
	}
	
	/**
	 * Load properties via a given stream.
	 * @throws Exception
	 */
	@Test
	public void testXmlConfigViaInputStream() throws Exception{
		// read off from an input stream
		BoneCPConfig config = new BoneCPConfig(this.getClass().getResourceAsStream("/bonecp-config.xml"), "specialApp");
		assertEquals(99, config.getMinConnectionsPerPartition());
	}
	
	/** XML based config.
	 * @throws Exception
	 */
	@Test
	public void testXMLConfigWithUnfoundSection() throws Exception{
		BoneCPConfig config = new BoneCPConfig("non-existant");
		assertEquals(20, config.getMinConnectionsPerPartition());
	}
	/**
	 * Test error condition for xml config.
	 */
	@Test
	public void testXmlConfigWithInvalidStream(){
		// throw errors
		try{
			new BoneCPConfig(null, "specialApp");
			fail("Should have thrown an exception");
		}catch (Exception e){
			// do nothing
		}
	}
	
	/** Tests configs using xml setups.
	 * @throws Exception
	 */
	@Test
	public void testPropertyBasedConfig() throws Exception{
		Properties props = new Properties();
		props.setProperty("minConnectionsPerPartition", "123");
		props.setProperty("bonecp.maxConnectionsPerPartition", "456");
		props.setProperty("idleConnectionTestPeriodInSeconds", "999");
		props.setProperty("username", "test");
		props.setProperty("partitionCount", "an int which is invalid");
		props.setProperty("idleMaxAgeInSeconds", "a long which is invalid");
		BoneCPConfig config = new BoneCPConfig(props);
		assertEquals(123, config.getMinConnectionsPerPartition());
		assertEquals(456, config.getMaxConnectionsPerPartition());
	}

	
	/**
	 * Property get/set
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGettersSetters(){
		Properties driverProperties = new Properties();
		DataSource mockDataSource = EasyMock.createNiceMock(DataSource.class);
		config.setJdbcUrl(CommonTestUtils.url);
		config.setUsername(CommonTestUtils.username);
		config.setPassword(CommonTestUtils.password);
		config.setIdleConnectionTestPeriod(60);
		config.setIdleMaxAge(60);
		config.setStatementsCacheSize(2);
		config.setReleaseHelperThreads(3);
		config.setMaxConnectionsPerPartition(5);
		config.setMinConnectionsPerPartition(5);
		config.setPartitionCount(1);
		config.setConnectionTestStatement("test");
		config.setAcquireIncrement(6);
		config.setInitSQL("abc");
		config.setDefaultTransactionIsolation("foo");
		config.setDefaultTransactionIsolationValue(123);
		config.setAcquireRetryDelay(1, TimeUnit.MINUTES);
		config.setConnectionTimeout(1, TimeUnit.MINUTES);
		config.setIdleMaxAge(1, TimeUnit.MINUTES);
		config.setIdleConnectionTestPeriod(1, TimeUnit.MINUTES);
		config.setMaxConnectionAge(1, TimeUnit.MINUTES);
		config.setDefaultReadOnly(true);
		config.setDefaultCatalog("foo");
		config.setDefaultAutoCommit(true);
		config.setStatisticsEnabled(true);
		
		assertEquals("foo", config.getDefaultCatalog());
		assertTrue(config.getDefaultAutoCommit());
		assertTrue(config.isStatisticsEnabled());
		assertTrue(config.getDefaultReadOnly());
		
		config.setMaxConnectionAge(60);
		assertEquals(60, config.getMaxConnectionAge());
		assertEquals(1, config.getIdleConnectionTestPeriod());
		assertEquals(1, config.getIdleMaxAge());
		assertEquals(60000, config.getConnectionTimeout());
		assertEquals(60, config.getConnectionTimeout(TimeUnit.SECONDS));
		
		assertEquals(60000, config.getAcquireRetryDelay());
		assertEquals("foo", config.getDefaultTransactionIsolation());
		assertEquals(123, config.getDefaultTransactionIsolationValue());
		
		ConnectionHook hook = new AbstractConnectionHook() {
			// do nothing
		};
		config.setConnectionHook(hook);
		
		config.setStatementsCachedPerConnection(7);
		config.setPreparedStatementsCacheSize(2);
		config.setStatementCacheSize(2);
		config.setPoolName("foo");
		config.setDisableJMX(false);
		config.setDatasourceBean(mockDataSource);
		config.setQueryExecuteTimeLimit(123);
		config.setDisableConnectionTracking(true);
		config.setConnectionTimeout(9999);
		config.setDriverProperties(driverProperties);
		config.setCloseConnectionWatchTimeout(Long.MAX_VALUE);
		String lifo = "LIFO";
		config.setServiceOrder(lifo);
		config.setConfigFile("abc");
		config.setIdleConnectionTestPeriodInMinutes(1);
		config.setConnectionTimeoutInMs(1000);
		config.setCloseConnectionWatchTimeoutInMs(1000);
		
		assertEquals("abc", config.getInitSQL());
		assertEquals(hook, config.getConnectionHook());
		assertEquals(1000, config.getConnectionTimeoutInMs());
		assertEquals(123, config.getQueryExecuteTimeLimit(TimeUnit.MILLISECONDS));
		assertEquals(1000, config.getCloseConnectionWatchTimeout(TimeUnit.MILLISECONDS));
		
		assertEquals(1000, config.getCloseConnectionWatchTimeoutInMs());
		assertEquals(1, config.getIdleConnectionTestPeriodInMinutes());
		assertEquals(lifo, config.getServiceOrder());
		assertEquals("abc", config.getConfigFile());
		assertEquals(1000, config.getCloseConnectionWatchTimeout());
		assertEquals("foo", config.getPoolName());
		assertEquals(CommonTestUtils.url, config.getJdbcUrl());
		assertEquals(CommonTestUtils.username, config.getUsername());
		assertEquals(CommonTestUtils.password, config.getPassword());
		assertEquals(2, config.getStatementsCacheSize());
		assertEquals(2, config.getStatementCacheSize());
		assertEquals(2, config.getPreparedStatementsCacheSize());
		assertEquals(2, config.getPreparedStatementCacheSize());
		assertEquals(3, config.getReleaseHelperThreads());
		assertEquals(5, config.getMaxConnectionsPerPartition());
		assertEquals(5, config.getMinConnectionsPerPartition());
		assertEquals(6, config.getAcquireIncrement());
		assertEquals(1000, config.getConnectionTimeout());
		assertEquals(true, config.isDisableConnectionTracking());
		assertEquals(7, config.getStatementsCachedPerConnection());
		assertEquals(123, config.getQueryExecuteTimeLimit());
		assertEquals(1, config.getPartitionCount());
		assertEquals("test", config.getConnectionTestStatement());
		assertEquals(mockDataSource, config.getDatasourceBean());
		assertEquals(driverProperties, config.getDriverProperties());
	}
	/**
	 * Config file scrubbing
	 */
	@Test
	public void testConfigSanitize(){
		config.setMaxConnectionsPerPartition(-1);
		config.setMinConnectionsPerPartition(-1);
		config.setPartitionCount(-1);
		config.setStatementsCacheSize(-1);
		config.setConnectionTestStatement("");
		config.setJdbcUrl(null);
		config.setUsername(null);
		config.setAcquireIncrement(0);
		config.setPassword(null);
		config.setPoolAvailabilityThreshold(-50);
		config.setStatementReleaseHelperThreads(-50);
		config.setConnectionTimeoutInMs(0);
		config.setServiceOrder("something non-sensical");
		config.setAcquireRetryDelayInMs(-1);
		
		config.setReleaseHelperThreads(-1);
		config.sanitize();

		assertEquals(1000, config.getAcquireRetryDelay(TimeUnit.MILLISECONDS));
		assertEquals(1000, config.getAcquireRetryDelayInMs());
		assertEquals("FIFO", config.getServiceOrder());
		assertEquals(0, config.getConnectionTimeoutInMs());
		assertNotNull(config.toString());
		assertEquals(3, config.getStatementReleaseHelperThreads());
		assertFalse(config.getAcquireIncrement() == 0);
		assertFalse(config.getReleaseHelperThreads() == -1);
		assertFalse(config.getMaxConnectionsPerPartition() == -1);
		assertFalse(config.getMinConnectionsPerPartition() == -1);
		assertFalse(config.getPartitionCount() == -1);
		assertFalse(config.getStatementsCacheSize() == -1);

		config.setMinConnectionsPerPartition(config.getMaxConnectionsPerPartition()+1);
		config.setServiceOrder(null);
		config.sanitize();
		assertEquals("FIFO", config.getServiceOrder());
		assertEquals(config.getMinConnectionsPerPartition(), config.getMaxConnectionsPerPartition());
		assertEquals(20, config.getPoolAvailabilityThreshold());
		
		config.setDefaultTransactionIsolation("NONE");
		config.sanitize();
		assertEquals(Connection.TRANSACTION_NONE, config.getDefaultTransactionIsolationValue());
		
		config.setDefaultTransactionIsolation("READ_COMMITTED");
		config.sanitize();
		assertEquals(Connection.TRANSACTION_READ_COMMITTED, config.getDefaultTransactionIsolationValue());
		
		config.setDefaultTransactionIsolation("READ_UNCOMMITTED");
		config.sanitize();
		assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, config.getDefaultTransactionIsolationValue());
		
		config.setDefaultTransactionIsolation("SERIALIZABLE");
		config.sanitize();
		assertEquals(Connection.TRANSACTION_SERIALIZABLE, config.getDefaultTransactionIsolationValue());
		
		config.setDefaultTransactionIsolation("REPEATABLE_READ");
		config.sanitize();
		assertEquals(Connection.TRANSACTION_REPEATABLE_READ, config.getDefaultTransactionIsolationValue());
		
		config.setDefaultTransactionIsolation("BAD_VALUE");
		config.sanitize();
		assertEquals(-1, config.getDefaultTransactionIsolationValue());
		
		// coverage
		BoneCPConfig config = new BoneCPConfig();
		config.setDatasourceBean(null);
		config.setDriverProperties(null);
		config.setJdbcUrl("");
		config.setPassword(null);
		config.sanitize();
	}
	
	/**
	 * Tests that setting driver properties handles username/password correctly.
	 */
	@Test
	public void testDriverPropertiesConfigSanitize(){
		config.setDatasourceBean(null);
		config.setUsername("foo");
		config.setPassword("bar");
		config.setMaxConnectionsPerPartition(2);
		config.setMinConnectionsPerPartition(2);
		config.setJdbcUrl("test");
		
		config.sanitize();
		
		Properties props = new Properties();
		props.setProperty("user", "something different");
		props.setProperty("password", "something different");
		config.setDriverProperties(props);
		config.sanitize();
		
		// if they don't match, the pool config wins
		assertEquals("foo", config.getDriverProperties().getProperty("user"));
		assertEquals("bar", config.getDriverProperties().getProperty("password"));

		
		
		config.setDriverProperties(new Properties());
		config.sanitize();
		
		// if not found, copied over from pool config
		assertEquals("foo", config.getDriverProperties().getProperty("user"));
		assertEquals("bar", config.getDriverProperties().getProperty("password"));
		
		
		config.setUsername(null);
		config.setPassword(null);
		config.setDriverProperties(new Properties());
		config.sanitize();
	}
	
	
	/**
	 * Tests that setting driver properties handles username/password correctly.
	 */
	@Test
	public void testDriverPropertiesConfigSanitize2(){
		config.setDatasourceBean(null);
		config.setUsername("foo");
		config.setPassword("bar");
		config.setMaxConnectionsPerPartition(2);
		config.setMinConnectionsPerPartition(2);
		config.setJdbcUrl("test");
		
		config.sanitize();
		
		Properties props = new Properties();
		config.setDriverProperties(props);
		config.sanitize();
		
		// if username/pass properties have been forgotten in driverProperties, set them
		assertEquals("foo", config.getDriverProperties().getProperty("user"));
		assertEquals("bar", config.getDriverProperties().getProperty("password"));
}
	
	/**
	 * Tests general methods.
	 * @throws CloneNotSupportedException 
	 */
	@Test
	public void testCloneEqualsHashCode() throws CloneNotSupportedException{
		BoneCPConfig clone = config.clone();
		assertTrue(clone.equals(config));
		assertEquals(clone.hashCode(), config.hashCode());
		
		assertFalse(clone.equals(null));
		assertTrue(clone.equals(clone));
		
		clone.setJdbcUrl("something else");
		assertFalse(clone.equals(config));
	}
	
	/**
	 * Tries to load an invalid property file.
	 * @throws CloneNotSupportedException 
	 * @throws IOException 
	 */
	@Test
	public void testLoadPropertyFileInvalid() throws CloneNotSupportedException, IOException{
		BoneCPConfig config = new BoneCPConfig();
		BoneCPConfig clone = config.clone();
		
		config.loadProperties("invalid-property-file.xml");
		assertEquals(config, clone);
	}

	/** See how the config handles a garbage filled file.
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 */
	@Test
	public void testLoadPropertyFileInvalid2() throws CloneNotSupportedException, IOException{
		BoneCPConfig config = new BoneCPConfig();
		BoneCPConfig clone = config.clone();
		
		config.loadProperties("java/lang/String.class");
		assertEquals(config, clone);
	}


}