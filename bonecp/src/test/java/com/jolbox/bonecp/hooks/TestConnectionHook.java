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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.hibernate.cfg.ExtendsQueueEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.CommonTestUtils;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.MockJDBCAnswer;
import com.jolbox.bonecp.MockJDBCDriver;

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
	/** Driver class. */
	private static MockJDBCDriver driver;
	/** Mock connection. */
	private static Connection mockConnection;

	/** Setups all mocks.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void setup() throws SQLException, ClassNotFoundException{
		hookClass = new CustomHook();
		mockConnection = createNiceMock(Connection.class);
		driver = new MockJDBCDriver(mockConnection); // load the driver
		mockConfig = createNiceMock(BoneCPConfig.class);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("jdbc:mock").anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(0).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(hookClass).anyTimes();
		replay(mockConfig);
		
		// once for no release threads, once with release threads....
		poolClass = new BoneCP(mockConfig);
	
	}
	
	/** Unload the driver.
	 * @throws SQLException
	 */
	@SuppressWarnings("static-access")
	@AfterClass
	public static void destroy() throws SQLException{
		driver.disable();
	}
	/**
	 * Killoff pool
	 */
	@AfterClass
	public static void shutdown(){
		poolClass.shutdown();
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
		mockConfig.sanitize();
		expectLastCall().anyTimes();

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
		mockConfig.sanitize();
		expectLastCall().anyTimes();

		driver.setConnection(null);
		driver.setMockJDBCAnswer(new MockJDBCAnswer() {
			
			public Connection answer() throws SQLException {
				throw new SQLException("Dummy error");
			}
		});
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("jdbc:mock").anyTimes();
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

	/**
	 * @throws SQLException
	 */
	@Test
	public void testOnAcquireFailDefault() throws SQLException {
		ConnectionHook hook = new AbstractConnectionHook() {
			// do nothing
		};
		AcquireFailConfig fail = createNiceMock(AcquireFailConfig.class);
		expect(fail.getAcquireRetryDelay()).andReturn(0).times(5).andThrow(new RuntimeException()).once();
		expect(fail.getAcquireRetryAttempts()).andReturn(new AtomicInteger(2)).times(3).andReturn(new AtomicInteger(1)).times(3);
		replay(fail);
		assertTrue(hook.onAcquireFail(new SQLException(), fail));
		assertFalse(hook.onAcquireFail(new SQLException(), fail));
		assertFalse(hook.onAcquireFail(new SQLException(), fail));
		
	}

	/**
	 * Test method.
	 * @throws SQLException 
	 */
	@Test
	public void testonQueryExecuteTimeLimitExceeded() throws SQLException {
		reset(mockConfig);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("jdbc:mock").anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(0).anyTimes();
		expect(mockConfig.isDisableConnectionTracking()).andReturn(true).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(hookClass).anyTimes();
		expect(mockConfig.getQueryExecuteTimeLimit()).andReturn(200).anyTimes();
		expect(mockConfig.getConnectionTimeout()).andReturn(Long.MAX_VALUE).anyTimes();
		
		PreparedStatement mockPreparedStatement = createNiceMock(PreparedStatement.class);
		expect(mockConnection.prepareStatement("")).andReturn(mockPreparedStatement).anyTimes();
		expect(mockPreparedStatement.execute()).andAnswer(new IAnswer<Boolean>() {
			
			public Boolean answer() throws Throwable {
				Thread.sleep(300); // something that exceeds our limit
				return false;
			}
		}).once();
		driver.setConnection(mockConnection);
		replay(mockConfig, mockPreparedStatement, mockConnection);
		
			poolClass = new BoneCP(mockConfig);	
			Connection con = poolClass.getConnection();
			con.prepareStatement("").execute();
			
		
		assertEquals(1, hookClass.queryTimeout);
		reset(mockConfig, mockPreparedStatement, mockConnection);
	}
	
	/**
	 * Test method.
	 * @throws SQLException 
	 */
	@Test
	public void testonQueryExecuteTimeLimitExceededCoverage() throws SQLException {
		reset(mockConfig, mockConnection);
		expect(mockConfig.getPartitionCount()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(5).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("jdbc:mock").anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(0).anyTimes();
		expect(mockConfig.isDisableConnectionTracking()).andReturn(true).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn( new CoverageHook()).anyTimes();
		expect(mockConfig.getQueryExecuteTimeLimit()).andReturn(200).anyTimes();
		
		PreparedStatement mockPreparedStatement = createNiceMock(PreparedStatement.class);
		expect(mockConnection.prepareStatement("")).andReturn(mockPreparedStatement).anyTimes();
		expect(mockPreparedStatement.execute()).andAnswer(new IAnswer<Boolean>() {
			
			public Boolean answer() throws Throwable {
				Thread.sleep(300); // something that exceeds our limit
				return false;
			}
		}).once();
		driver.setConnection(mockConnection);
		replay(mockConfig, mockPreparedStatement, mockConnection);
		
			poolClass = new BoneCP(mockConfig);	
			Connection con = poolClass.getConnection();
			con.prepareStatement("").execute();
			
		
		assertEquals(1, hookClass.queryTimeout);
	}
}
