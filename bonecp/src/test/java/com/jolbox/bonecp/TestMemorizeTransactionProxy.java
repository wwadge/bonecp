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
package com.jolbox.bonecp;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Wallace
 *
 */
public class TestMemorizeTransactionProxy {
	
	@BeforeClass
	public static void enableMockDriver() throws SQLException{
		MockJDBCDriver.disable(false);
	}
	
	@AfterClass
	public static void disableMockDriver() throws SQLException{
		MockJDBCDriver.disable(true);
	}

	/** Tests that the statements and connections are proxified.
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testProxies() throws SQLException, SecurityException, IllegalArgumentException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException{
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		CallableStatement mockCallableStatement = createNiceMock(CallableStatement.class);

		new MockJDBCDriver(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(true);
		String jdbcDriver = CommonTestUtils.config.getJdbcUrl();
		CommonTestUtils.config.setJdbcUrl("jdbc:mock:driver");
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		CommonTestUtils.config.setTransactionRecoveryEnabled(true);
		
		expect(mockConnection.prepareCall("")).andReturn(mockCallableStatement).anyTimes();
		replay(mockConnection);
		Connection con = pool.getConnection();
		PreparedStatement ps = con.prepareStatement("");
		CallableStatement cs = con.prepareCall("");
		
		Statement stmt = con.createStatement();
		
		
		InvocationHandler handler = java.lang.reflect.Proxy.getInvocationHandler( ((ConnectionHandle) con).getInternalConnection());
		assertEquals(MemorizeTransactionProxy.class, handler.getClass());
		
		Field field = PreparedStatementHandle.class.getDeclaredField("internalPreparedStatement");
		field.setAccessible(true);
		ps = (PreparedStatement) field.get(ps);
		
		handler = java.lang.reflect.Proxy.getInvocationHandler(ps);
		assertEquals(MemorizeTransactionProxy.class, handler.getClass());
		
		field = CallableStatementHandle.class.getDeclaredField("internalCallableStatement");
		field.setAccessible(true);
		cs = (CallableStatement) field.get(cs);
		
		handler = java.lang.reflect.Proxy.getInvocationHandler(cs);
		assertEquals(MemorizeTransactionProxy.class, handler.getClass());

		field = StatementHandle.class.getDeclaredField("internalStatement");
		field.setAccessible(true);
		stmt = (Statement) field.get(stmt);

		handler = java.lang.reflect.Proxy.getInvocationHandler(stmt);
		assertEquals(MemorizeTransactionProxy.class, handler.getClass());
		CommonTestUtils.config.setJdbcUrl(jdbcDriver);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);
		
	}
	
//	public void test
}
