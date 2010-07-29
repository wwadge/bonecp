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
package com.jolbox.bonecp.spring;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.MockJDBCDriver;
/**
 * Tests DynamicDataSourceProxy.
 * @author Wallace
 *
 */
public class TestDynamicDataSourceProxy {

	/**
	 * @throws ClassNotFoundException
	 * @throws SQLException 
	 */
	@BeforeClass
	public static void setup() throws ClassNotFoundException, SQLException{
		Class.forName("com.jolbox.bonecp.MockJDBCDriver");
		new MockJDBCDriver();
	}
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
		config.setJdbcUrl("jdbc:mock");
		config.setUsername("sa");
		config.setPassword("");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setPartitionCount(1);
		

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
