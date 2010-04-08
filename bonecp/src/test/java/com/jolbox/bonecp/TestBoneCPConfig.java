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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

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
	
	/**
	 * Property get/set
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGettersSetters(){
		config.setJdbcUrl(CommonTestUtils.url);
		config.setUsername(CommonTestUtils.username);
		config.setPassword(CommonTestUtils.password);
		config.setIdleConnectionTestPeriod(1);
		config.setIdleMaxAge(1);
		config.setStatementsCacheSize(2);
		config.setReleaseHelperThreads(3);
		config.setMaxConnectionsPerPartition(5);
		config.setMinConnectionsPerPartition(5);
		config.setPartitionCount(1);
		config.setConnectionTestStatement("test");
		config.setAcquireIncrement(6);
		config.setStatementsCachedPerConnection(7);
		config.setPreparedStatementsCacheSize(2);
		config.setPoolName("foo");
		config.setDisableJMX(false);

		assertEquals("foo", config.getPoolName());
		assertEquals(CommonTestUtils.url, config.getJdbcUrl());
		assertEquals(CommonTestUtils.username, config.getUsername());
		assertEquals(CommonTestUtils.password, config.getPassword());
		assertEquals(60*1000, config.getIdleConnectionTestPeriod());
		assertEquals(60*1000, config.getIdleMaxAge());
		assertEquals(2, config.getStatementsCacheSize());
		assertEquals(2, config.getPreparedStatementsCacheSize());
		assertEquals(3, config.getReleaseHelperThreads());
		assertEquals(5, config.getMaxConnectionsPerPartition());
		assertEquals(5, config.getMinConnectionsPerPartition());
		assertEquals(6, config.getAcquireIncrement());
		assertEquals(7, config.getStatementsCachedPerConnection());
		assertEquals(1, config.getPartitionCount());
		assertEquals("test", config.getConnectionTestStatement());
				


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
		
		config.setReleaseHelperThreads(-1);
		config.sanitize();

		assertNotNull(config.toString());
		assertFalse(config.getAcquireIncrement() == 0);
		assertFalse(config.getReleaseHelperThreads() == -1);
		assertFalse(config.getMaxConnectionsPerPartition() == -1);
		assertFalse(config.getMinConnectionsPerPartition() == -1);
		assertFalse(config.getPartitionCount() == -1);
		assertFalse(config.getStatementsCacheSize() == -1);

		config.setMinConnectionsPerPartition(config.getMaxConnectionsPerPartition()+1);
		config.sanitize();
		assertEquals(config.getMinConnectionsPerPartition(), config.getMaxConnectionsPerPartition());

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


}
