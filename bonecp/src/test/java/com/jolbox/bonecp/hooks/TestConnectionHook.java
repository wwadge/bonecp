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
package com.jolbox.bonecp.hooks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.CommonTestUtils;

/** Tests the connection hooks. 
 * @author wallacew
 *
 */
public class TestConnectionHook {

	/** Mock support. */
	private static BoneCPConfig mockConfig;
	/** Pool handle. */
	private static BoneCP poolClass;
	/** Helper class. */
	private static CustomHook hookClass;

	/** Setups all mocks.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void setup() throws SQLException, ClassNotFoundException{
		hookClass = new CustomHook();
		Class.forName("org.hsqldb.jdbcDriver");
		mockConfig = createNiceMock(BoneCPConfig.class);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn(CommonTestUtils.url).anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(0).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(hookClass).anyTimes();
		replay(mockConfig);
		
		// once for no release threads, once with release threads....
		poolClass = new BoneCP(mockConfig);
	
	}
	/**
	 * Test method for {@link com.jolbox.bonecp.hooks.AbstractConnectionHook#onAcquire(com.jolbox.bonecp.ConnectionHandle)}.
	 */
	@Test
	public void testOnAcquire() {
		assertEquals(5, hookClass.acquire);
	}


	/**
	 * Test method for {@link com.jolbox.bonecp.hooks.AbstractConnectionHook#onCheckOut(com.jolbox.bonecp.ConnectionHandle)}.
	 * @throws SQLException 
	 */
	@Test
	public void testOnCheckOutAndOnCheckin() throws SQLException {
		poolClass.getConnection().close();
		assertEquals(1, hookClass.checkout);
		assertEquals(1, hookClass.checkin);
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.hooks.AbstractConnectionHook#onDestroy(com.jolbox.bonecp.ConnectionHandle)}.
	 */
	@Test
	public void testOnDestroy() {
		poolClass.close();
		assertEquals(5, hookClass.destroy);
	}

	
	
	/** Just to do code coverage of abstract class.
	 * @throws SQLException
	 */
	@Test
	public void dummyCoverage() throws SQLException{
		CoverageHook hook = new CoverageHook();
		reset(mockConfig);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn(CommonTestUtils.url).anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(0).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(hook).anyTimes();
		replay(mockConfig);
		
		poolClass = new BoneCP(mockConfig);	
		
		poolClass.getConnection().close();
		poolClass.close();
		
		reset(mockConfig);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(0).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(hook).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("something-bad").anyTimes();
		replay(mockConfig);
		try{
			poolClass = new BoneCP(mockConfig);
			// should throw an exception
		} catch (Exception e){
			// do nothing
		}

	}
	
	/**
	 * Test method for {@link com.jolbox.bonecp.hooks.AbstractConnectionHook#onDestroy(com.jolbox.bonecp.ConnectionHandle)}.
	 * @throws SQLException 
	 */
	@Test
	public void testOnAcquireFail() throws SQLException {
		hookClass = new CustomHook();
		reset(mockConfig);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("somethingbad").anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(0).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(hookClass).anyTimes();
		replay(mockConfig);
		
		try{
			poolClass = new BoneCP(mockConfig);	
			poolClass.getConnection();
		} catch (Exception e){
			// do nothing
		}
		assertEquals(1, hookClass.fail);
		
	}
}
