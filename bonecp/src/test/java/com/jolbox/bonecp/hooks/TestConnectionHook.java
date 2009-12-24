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
		poolClass.releaseConnection(poolClass.getConnection());
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
		
		poolClass.releaseConnection(poolClass.getConnection());
		poolClass.close();
	}
}
