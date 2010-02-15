/**
 * 
 */
package com.jolbox.bonecp.spring;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.CommonTestUtils;
/**
 * Tests DynamicDataSourceProxy.
 * @author Wallace
 *
 */
public class TestDynamicDataSourceProxy {

	/**
	 * Test method for {@link com.jolbox.bonecp.spring.DynamicDataSourceProxy#DynamicDataSourceProxy(javax.sql.DataSource)}.
	 */
	@Test
	public void testDynamicDataSourceProxyDataSource() {
		DelegatingDataSource mockDataSource = createNiceMock(DelegatingDataSource.class);
		DynamicDataSourceProxy ddsp = new DynamicDataSourceProxy(mockDataSource);
		assertEquals(mockDataSource, ddsp.getTargetDataSource());
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.spring.DynamicDataSourceProxy#DynamicDataSourceProxy()}.
	 */
	@Test
	public void testDynamicDataSourceProxy() {
		new DynamicDataSourceProxy(); // coverage
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.spring.DynamicDataSourceProxy#switchDataSource(com.jolbox.bonecp.BoneCPConfig)}.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testSwitchDataSource() throws SQLException, ClassNotFoundException {
		BoneCPDataSource mockDataSource = createNiceMock(BoneCPDataSource.class);
		DynamicDataSourceProxy ddsp = new DynamicDataSourceProxy();
		// Test #1: Check for correct instance.
		ddsp.setTargetDataSource(new DelegatingDataSource());
		try{
			ddsp.switchDataSource(null);
			fail("Should throw an exception");
		} catch(SQLException e){
			// do nothing
		}
		
		// Test #2: Given a good config, should initialize pool and switch datasource to it
		BoneCPConfig config = new BoneCPConfig();
		config.setJdbcUrl(CommonTestUtils.url);
		config.setUsername(CommonTestUtils.username);
		config.setPassword(CommonTestUtils.password);
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setPartitionCount(1);
		Class.forName("org.hsqldb.jdbcDriver");

		ddsp = new DynamicDataSourceProxy(mockDataSource);

		// Old datasource should be closed.
		mockDataSource.close();
		expectLastCall().once();
		replay(mockDataSource);
		ddsp.switchDataSource(config);
		// and a new datasource should be in place
		assertNotSame(mockDataSource, ddsp.getTargetDataSource());
		verify(mockDataSource);
	}

}
