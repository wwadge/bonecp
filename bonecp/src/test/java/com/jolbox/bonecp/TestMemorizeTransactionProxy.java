/**
 * 
 */
package com.jolbox.bonecp;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.expectLastCall;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

/**
 * @author Wallace
 *
 */
public class TestMemorizeTransactionProxy {
	

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

		MockJDBCDriver mockDriver = new MockJDBCDriver(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(true);
		CommonTestUtils.config.setJdbcUrl("jdbc:mock:driver");
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);
		
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
		mockDriver.disable();
	}
	
	static int count = 1;
	Connection mockConnection = createNiceMock(Connection.class);
	// make it return a new connection when asked for again
	Connection mockConnection2 = createNiceMock(Connection.class);

	@Test
	public void testReplayTransaction() throws IllegalArgumentException, Throwable{
		PreparedStatement mockPreparedStatement = createNiceMock(PreparedStatement.class);

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			@Override
			public Connection answer() throws SQLException {
				if (count == 1){
					return TestMemorizeTransactionProxy.this.mockConnection;
				}
				return TestMemorizeTransactionProxy.this.mockConnection2;
			}
		});
		
		
		CommonTestUtils.config.setTransactionRecoveryEnabled(true);
		CommonTestUtils.config.setJdbcUrl("jdbc:mock:driver");
		CommonTestUtils.config.setMinConnectionsPerPartition(2);
		CommonTestUtils.config.setMaxConnectionsPerPartition(2);
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);

		EasyMock.makeThreadSafe(mockConnection, true);
		expect(mockConnection.prepareStatement("whatever")).andReturn(mockPreparedStatement).anyTimes();
		
		
		expect(mockPreparedStatement.execute()).andThrow(new SQLException("", "08S01")).once();
		// This connection should be closed off
		mockConnection.close(); // remember that this is the internal connection
		expectLastCall().once();

		
		// we should be getting a new connection and everything replayed on it
		PreparedStatement mockPreparedStatement2 = createStrictMock(PreparedStatement.class);
		expect(mockConnection2.prepareStatement("whatever")).andReturn(mockPreparedStatement2).anyTimes();
		mockPreparedStatement2.setInt(1, 1);
		expectLastCall().once();
		expect(mockPreparedStatement2.execute()).andReturn(true).once();
		
		
		replay(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		Connection con = pool.getConnection();
		count=0;

		mockDriver.setConnection(mockConnection2);
		
		PreparedStatement ps = con.prepareStatement("whatever");
		ps.setInt(1, 1);
		ps.execute();               
		
		
		mockDriver.disable();
		
	}
}
