/**
 * 
 */
package com.jolbox.bonecp;

import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.anyObject;
import static org.easymock.classextension.EasyMock.expectLastCall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.easymock.classextension.EasyMock.makeThreadSafe;

/** Tests config object.
 * @author wwadge
 *
 */
public class TestBoneCPConfig {
	
	/** Mock handle. */
	private static Logger mockLogger;
	
	/** Stub out any calls to logger.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@BeforeClass
	public static void setup() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		mockLogger = createNiceMock(Logger.class);
		makeThreadSafe(mockLogger, true);
		Field field = CommonTestUtils.config.getClass().getDeclaredField("logger");
		field.setAccessible(true);
		field.set(CommonTestUtils.config, mockLogger);
		mockLogger.error(anyObject());
		expectLastCall().anyTimes();
		mockLogger.warn(anyObject());
		expectLastCall().anyTimes();
		replay(mockLogger);
	}
	
	/**
	 * Property get/set
	 */
	@Test
	public void testGettersSetters(){
		CommonTestUtils.config.setJdbcUrl(CommonTestUtils.url);
		CommonTestUtils.config.setUsername(CommonTestUtils.username);
		CommonTestUtils.config.setPassword(CommonTestUtils.password);
		CommonTestUtils.config.setIdleConnectionTestPeriod(10000);
		CommonTestUtils.config.setIdleMaxAge(1);
		CommonTestUtils.config.setPreparedStatementsCacheSize(2);
		CommonTestUtils.config.setReleaseHelperThreads(3);
		CommonTestUtils.config.setMaxConnectionsPerPartition(5);
		CommonTestUtils.config.setMinConnectionsPerPartition(5);
		CommonTestUtils.config.setPartitionCount(1);
		CommonTestUtils.config.setConnectionTestStatement("test");
		CommonTestUtils.config.setAcquireIncrement(6);
		CommonTestUtils.config.setStatementsCachedPerConnection(7);

		
		assertEquals(CommonTestUtils.url, CommonTestUtils.config.getJdbcUrl());
		assertEquals(CommonTestUtils.username, CommonTestUtils.config.getUsername());
		assertEquals(CommonTestUtils.password, CommonTestUtils.config.getPassword());
		assertEquals(10000, CommonTestUtils.config.getIdleConnectionTestPeriod());
		assertEquals(1, CommonTestUtils.config.getIdleMaxAge());
		assertEquals(2, CommonTestUtils.config.getPreparedStatementsCacheSize());
		assertEquals(3, CommonTestUtils.config.getReleaseHelperThreads());
		assertEquals(5, CommonTestUtils.config.getMaxConnectionsPerPartition());
		assertEquals(5, CommonTestUtils.config.getMinConnectionsPerPartition());
		assertEquals(6, CommonTestUtils.config.getAcquireIncrement());
		assertEquals(7, CommonTestUtils.config.getStatementsCachedPerConnection());
		assertEquals(1, CommonTestUtils.config.getPartitionCount());
		assertEquals("test", CommonTestUtils.config.getConnectionTestStatement());
				


	}
	/**
	 * Config file scrubbing
	 */
	@Test
	public void testConfigSanitize(){
		CommonTestUtils.config.setMaxConnectionsPerPartition(-1);
		CommonTestUtils.config.setMinConnectionsPerPartition(-1);
		CommonTestUtils.config.setPartitionCount(-1);
		CommonTestUtils.config.setPreparedStatementsCacheSize(-1);
		CommonTestUtils.config.setStatementsCachedPerConnection(-1);
		CommonTestUtils.config.setConnectionTestStatement("");
		CommonTestUtils.config.setJdbcUrl(null);
		CommonTestUtils.config.setUsername(null);
		CommonTestUtils.config.setAcquireIncrement(0);
		CommonTestUtils.config.setPassword(null);
		
		CommonTestUtils.config.setReleaseHelperThreads(-1);
		CommonTestUtils.config.sanitize();

		assertNotNull(CommonTestUtils.config.toString());
		assertFalse(CommonTestUtils.config.getAcquireIncrement() == 0);
		assertFalse(CommonTestUtils.config.getReleaseHelperThreads() == -1);
		assertFalse(CommonTestUtils.config.getMaxConnectionsPerPartition() == -1);
		assertFalse(CommonTestUtils.config.getMinConnectionsPerPartition() == -1);
		assertFalse(CommonTestUtils.config.getPartitionCount() == -1);
		assertFalse(CommonTestUtils.config.getPreparedStatementsCacheSize() == -1);
		assertFalse(CommonTestUtils.config.getStatementsCachedPerConnection() == -1);

		CommonTestUtils.config.setMinConnectionsPerPartition(CommonTestUtils.config.getMaxConnectionsPerPartition()+1);
		CommonTestUtils.config.sanitize();
		assertEquals(CommonTestUtils.config.getMinConnectionsPerPartition(), CommonTestUtils.config.getMaxConnectionsPerPartition());

	}


}
