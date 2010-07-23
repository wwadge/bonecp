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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.fail;

import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.hooks.CoverageHook;
import com.jolbox.bonecp.proxy.TransactionRecoveryResult;

/**
 * @author Wallace
 *
 */
public class TestMemorizeTransactionProxy {
	/** Mock handle. */
	static ConnectionHandle mockConnection;
	/** Mock handle. */
	static Connection mockConnection2;
	/** Mock handle. */
	static CallableStatement mockCallableStatement;
	/** Mock handle. */
	static Statement mockStatement;
	/** Mock handle. */
	static PreparedStatement mockPreparedStatement;
	/** Mock handle. */
	static Connection mockConnection3;
	/** Config handle. */
	private static BoneCPConfig config; 
	/**
	 * Test setup.
	 */
	@BeforeClass
	public static void beforeClass(){
		config = CommonTestUtils.getConfigClone();
		config.setDisableConnectionTracking(true);
		mockConnection = createNiceMock(ConnectionHandle.class);
		// make it return a new connection when asked for again
		mockConnection2 = createNiceMock(Connection.class);
		mockConnection3 = createNiceMock(Connection.class);
		mockCallableStatement = createNiceMock(CallableStatement.class);
		mockStatement = createNiceMock(Statement.class);
		mockPreparedStatement = createNiceMock(PreparedStatement.class);
	}
	/**
	 * Test reset.
	 */
	@Before
	public void before(){
		reset(mockConnection, mockConnection2, mockConnection3, mockCallableStatement, mockPreparedStatement, mockStatement);
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
		

		MockJDBCDriver mockDriver = new MockJDBCDriver(mockConnection);
		config.setTransactionRecoveryEnabled(true);
		config.setJdbcUrl("jdbc:mock:driver2");
		config.setReleaseHelperThreads(0);
		BoneCP pool = new BoneCP(config);
		config.setTransactionRecoveryEnabled(false);
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

		
		// fake stuff to test for clear
		((ConnectionHandle)con).getReplayLog().add(new ReplayLog(null, null, null));
		((ConnectionHandle)con).recoveryResult.getReplaceTarget().put("test", "test1");
		con.rollback(); // should clear out log
		assertTrue(((ConnectionHandle)con).getReplayLog().isEmpty());
//		assertTrue(((ConnectionHandle)con).recoveryResult.getReplaceTarget().isEmpty());

		// fake stuff to test for clear
		((ConnectionHandle)con).getReplayLog().add(new ReplayLog(null, null, null));
		((ConnectionHandle)con).recoveryResult.getReplaceTarget().put("test", "test1");
		con.commit(); // should clear out log
		assertTrue(((ConnectionHandle)con).getReplayLog().isEmpty());
//		assertTrue(((ConnectionHandle)con).recoveryResult.getReplaceTarget().isEmpty());

		assertNotNull(((ConnectionHandle)con).getProxyTarget());
		try{
			field = con.getClass().getDeclaredField("connection");
			field.setAccessible(true);
			field.set(con, null);
			((ConnectionHandle)con).getProxyTarget();
			fail("should have thrown an exception");
		} catch (Throwable t){
			// should throw an exception
		}
		mockDriver.disable();
	}
	
	/** Temp. */
	static int count = 1;

	
	/** Test of normal replay functionality. 
	 * @throws IllegalArgumentException
	 * @throws Throwable
	 */
	@Test
	public void testReplayTransaction() throws IllegalArgumentException, Throwable{
		
		count = 1;

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			// @Override
			public Connection answer() throws SQLException {
				if (count == 1){
					return TestMemorizeTransactionProxy.mockConnection;
				}
				return TestMemorizeTransactionProxy.mockConnection2;
			}
		}); 
		
		
		config.setTransactionRecoveryEnabled(true);
		config.setJdbcUrl("jdbc:mock:driver");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		BoneCP pool = new BoneCP(config);
		reset(mockConnection);
		config.setTransactionRecoveryEnabled(false);

		EasyMock.makeThreadSafe(mockConnection, true);
		String prepCall = "whatever2";
		expect(mockConnection.prepareStatement("whatever")).andReturn(mockPreparedStatement).anyTimes();
		expect(mockConnection.prepareCall(prepCall)).andReturn(mockCallableStatement).anyTimes();
		expect(mockConnection.createStatement()).andReturn(mockStatement).anyTimes();
		
		// trigger a replay
		expect(mockPreparedStatement.execute()).andThrow(new SQLException("", "08S01")).once();
		
		// This connection should be closed off
		mockConnection.close(); // remember that this is the internal connection
		expectLastCall().once();
//		.andThrow(new SQLException("just a fake exception for code coverage")).once();

		
		// we should be getting a new connection and everything replayed on it
		PreparedStatement mockPreparedStatement2 = createStrictMock(PreparedStatement.class);
		expect(mockConnection2.prepareStatement("whatever")).andReturn(mockPreparedStatement2).anyTimes();
		mockPreparedStatement2.setInt(1, 1);
		expectLastCall().once();
		expect(mockPreparedStatement2.execute()).andReturn(true).once();
		
		CallableStatement mockCallableStatement2 = createStrictMock(CallableStatement.class);
		expect(mockConnection2.prepareCall(prepCall)).andReturn(mockCallableStatement2).anyTimes();
		mockCallableStatement2.clearWarnings();
		expectLastCall().once();
		
		Statement mockStatement2 = createStrictMock(Statement.class);
		expect(mockConnection2.createStatement()).andReturn(mockStatement2).anyTimes();
		mockStatement2.clearWarnings();
		expectLastCall().once();
		
		replay(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2, mockCallableStatement2, mockStatement, mockStatement2);
		
		Connection con = pool.getConnection();

		((ConnectionHandle)con).recoveryResult=new TransactionRecoveryResult(); 		// for code coverage
		((ConnectionHandle)con).recoveryResult.getReplaceTarget().put(mockConnection, mockConnection); 		// for code coverage
		((ConnectionHandle)con).recoveryResult.getReplaceTarget().put(con, con);		// for code coverage

		count=0;

		mockDriver.setConnection(mockConnection2);
		
		PreparedStatement ps = con.prepareStatement("whatever");
		ps.setInt(1, 1);
		CallableStatement cs = con.prepareCall(prepCall);
		cs.clearWarnings();
		Statement stmt = con.createStatement();
		stmt.clearWarnings();
		ps.execute();    
		con.close();
		
		verify(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2, mockCallableStatement2, mockStatement, mockStatement2);
		
		mockDriver.disable();
	}
	
	
	/** Make the proxy fail but via a normal (non-recoverable) application error eg bad parameters passed to a preparedStatement.
	 * @throws IllegalArgumentException
	 * @throws Throwable
	 */
	@Test
	public void testReplayTransactionWithUserError() throws IllegalArgumentException, Throwable{
		
		count = 1;

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			// @Override
			public Connection answer() throws SQLException {
					return TestMemorizeTransactionProxy.mockConnection;
			}
		}); 
		
		
		config.setTransactionRecoveryEnabled(true);
		config.setJdbcUrl("jdbc:mock:driver");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		BoneCP pool = new BoneCP(config);
		reset(mockConnection);
		config.setTransactionRecoveryEnabled(false);

		EasyMock.makeThreadSafe(mockConnection, true);
		expect(mockConnection.prepareStatement("whatever")).andReturn(mockPreparedStatement).anyTimes();
		expect(mockPreparedStatement.execute()).andThrow(new SQLException("Fake user-error", "123")).once();
		
		replay(mockConnection, mockPreparedStatement);
		
		Connection con = pool.getConnection();
		count=0;

		PreparedStatement ps = con.prepareStatement("whatever");
		ps.setInt(999, 1); // should throw an error in real-life
		try{
			ps.execute();
			fail("Should have thrown an error");
		} catch(SQLException e){
			// expected
		}
		
		
		verify(mockConnection, mockPreparedStatement);
		mockDriver.disable();
		
	}
	
	/** Fail, then fail again on replay to see that it recovers.
	 * @throws IllegalArgumentException
	 * @throws Throwable
	 */
	@Test
	public void testReplayTransactionWithFailuresOnReplay() throws IllegalArgumentException, Throwable{
		
		count = 1;

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			// @Override
			public Connection answer() throws SQLException {
				if (count == 1){
					return TestMemorizeTransactionProxy.mockConnection;
				}  if (count == 0){
					count--;
					return TestMemorizeTransactionProxy.mockConnection2;
				} 
					return TestMemorizeTransactionProxy.mockConnection3;
			}
		}); 
		
		
		config.setTransactionRecoveryEnabled(true);
		config.setJdbcUrl("jdbc:mock:driver");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setAcquireRetryDelay(1);
		config.setAcquireRetryAttempts(2);
//		config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(config);
		reset(mockConnection);
		config.setTransactionRecoveryEnabled(false);

		EasyMock.makeThreadSafe(mockConnection, true);
		// we should be getting new connections and everything replayed on it
		PreparedStatement mockPreparedStatement2 = createStrictMock(PreparedStatement.class);
		PreparedStatement mockPreparedStatement3 = createStrictMock(PreparedStatement.class);
		
		expect(mockConnection.prepareStatement("whatever")).andReturn(mockPreparedStatement).once();
		expect(mockConnection2.prepareStatement("whatever")).andReturn(mockPreparedStatement2).once();
		expect(mockConnection3.prepareStatement("whatever")).andReturn(mockPreparedStatement3).once();
		
		// trigger a replay
		expect(mockPreparedStatement.execute()).andThrow(new SQLException("", "08S01")).once();
		 
		// This connection should be closed off
		mockConnection.close(); // remember that this is the internal connection
		expectLastCall().andThrow(new SQLException("just a fake exception for code coverage")).anyTimes();
		mockConnection2.close(); // remember that this is the internal connection
		mockConnection3.close(); // remember that this is the internal connection
		

		
		
		mockPreparedStatement2.setInt(1, 1);
		expect(mockPreparedStatement2.execute()).andThrow(new SQLException("Fake errors to force replaying")).once();
		
		mockPreparedStatement3.setInt(1, 1);
		expect(mockPreparedStatement3.execute()).andReturn(true).once();
				
		replay(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2, mockPreparedStatement3, mockConnection3);
		
		Connection con = pool.getConnection();
		count=0;

//		mockDriver.setConnection(mockConnection2);
		
		PreparedStatement ps = con.prepareStatement("whatever");
		ps.setInt(1, 1);
		ps.execute();               
		
		verify(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2, mockPreparedStatement3);
		
		mockDriver.disable();
	}
	
	
	/** Test to see what happens when a replay keeps failing.
	 * @throws IllegalArgumentException
	 * @throws Throwable
	 */
	@Test
	public void testReplayTransactionWithFailuresOnReplayKeepFailing() throws IllegalArgumentException, Throwable{
		
		count = 1;

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			// @Override
			public Connection answer() throws SQLException {
				if (count == 1){
					return TestMemorizeTransactionProxy.mockConnection;
				}  
					return TestMemorizeTransactionProxy.mockConnection2;
			}
		}); 
		
		
		config.setTransactionRecoveryEnabled(true);
		config.setJdbcUrl("jdbc:mock:driver");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setAcquireRetryDelay(1);
		config.setAcquireRetryAttempts(1);
//		config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(config);
		reset(mockConnection);
		config.setTransactionRecoveryEnabled(false);

		EasyMock.makeThreadSafe(mockConnection, true);
		// we should be getting new connections and everything replayed on it
		PreparedStatement mockPreparedStatement2 = createStrictMock(PreparedStatement.class);
		
		expect(mockConnection.prepareStatement("whatever")).andReturn(mockPreparedStatement).once();
		expect(mockConnection2.prepareStatement("whatever")).andReturn(mockPreparedStatement2).once();
		
		// trigger a replay
		expect(mockPreparedStatement.execute()).andThrow(new SQLException("", "08S01")).once();
		 
		// This connection should be closed off
		mockConnection.close(); // remember that this is the internal connection
		expectLastCall().andThrow(new SQLException("just a fake exception for code coverage")).anyTimes();
		

		
		
		mockPreparedStatement2.setInt(1, 1);
		expect(mockPreparedStatement2.execute()).andThrow(new SQLException("Fake errors to force replaying")).once();
		
		replay(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		Connection con = pool.getConnection();
		count=0;

//		mockDriver.setConnection(mockConnection2);
		
		PreparedStatement ps = con.prepareStatement("whatever");
		ps.setInt(1, 1);
		try{
			ps.execute();
			fail("Should have thrown exception");
		} catch (SQLException e){
//			e.printStackTrace();
			// expected exception
		}
		
		
		verify(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		mockDriver.disable();
	}
	
	/** Interrupted
	 * @throws IllegalArgumentException
	 * @throws Throwable
	 */
	@Test
	public void testReplayTransactionWithFailuresInterruptedException() throws IllegalArgumentException, Throwable{
		
		count = 1;

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			// @Override
			public Connection answer() throws SQLException {
				if (count == 1){
					return TestMemorizeTransactionProxy.mockConnection;
				}  
					return TestMemorizeTransactionProxy.mockConnection2;
			}
		}); 
		
		
		config.setTransactionRecoveryEnabled(true);
		config.setJdbcUrl("jdbc:mock:driver");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setAcquireRetryDelay(10000);
		config.setAcquireRetryAttempts(2);
//		config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(config);
		reset(mockConnection);
		config.setTransactionRecoveryEnabled(false);

		EasyMock.makeThreadSafe(mockConnection, true);
		// we should be getting new connections and everything replayed on it
		PreparedStatement mockPreparedStatement2 = createStrictMock(PreparedStatement.class);
		
		expect(mockConnection.prepareStatement("whatever")).andReturn(mockPreparedStatement).once();
		expect(mockConnection2.prepareStatement("whatever")).andReturn(mockPreparedStatement2).once();
		
		// trigger a replay
		expect(mockPreparedStatement.execute()).andThrow(new SQLException("", "08S01")).once();
		 
		// This connection should be closed off
		mockConnection.close(); // remember that this is the internal connection
		expectLastCall().andThrow(new SQLException("just a fake exception for code coverage")).anyTimes();
		

		
		
		mockPreparedStatement2.setInt(1, 1);
		expect(mockPreparedStatement2.execute()).andThrow(new SQLException("Fake errors to force replaying")).once();
		
		replay(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		Connection con = pool.getConnection();
		count=0;

		final Thread currentThread = Thread.currentThread();

			new Thread(new Runnable() {
				
				// @Override
				public void run() {
					while (!currentThread.getState().equals(State.TIMED_WAITING)){
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					currentThread.interrupt();
				}
			}).start();
		
		
		PreparedStatement ps = con.prepareStatement("whatever");
		ps.setInt(1, 1);
		try{
			ps.execute();
			fail("Should have thrown exception");
		} catch (SQLException e){
			// expected exception
		}
		
		
		verify(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		mockDriver.disable();
	}
	
	/** Test hooks.
	 * @throws IllegalArgumentException
	 * @throws Throwable
	 */
	@Test
	public void testReplayTransactionWithFailuresCustomHook() throws IllegalArgumentException, Throwable{
		
		count = 1;

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			// @Override
			public Connection answer() throws SQLException {
				if (count == 1){
					return TestMemorizeTransactionProxy.mockConnection;
				}  
					return TestMemorizeTransactionProxy.mockConnection2;
			}
		}); 
		
		
		config.setTransactionRecoveryEnabled(true);
		config.setJdbcUrl("jdbc:mock:driver");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setAcquireRetryDelay(1);
		config.setAcquireRetryAttempts(2);
		config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(config);
		reset(mockConnection);
		config.setTransactionRecoveryEnabled(false);

		EasyMock.makeThreadSafe(mockConnection, true);
		// we should be getting new connections and everything replayed on it
		PreparedStatement mockPreparedStatement2 = createStrictMock(PreparedStatement.class);
		
		expect(mockConnection.prepareStatement("whatever")).andReturn(mockPreparedStatement).once();
		expect(mockConnection2.prepareStatement("whatever")).andReturn(mockPreparedStatement2).once();
		
		// trigger a replay
		expect(mockPreparedStatement.execute()).andThrow(new SQLException("", "08S01")).once();
		 
		// This connection should be closed off
		mockConnection.close(); // remember that this is the internal connection
		expectLastCall().andThrow(new SQLException("just a fake exception for code coverage")).anyTimes();
		

		
		
		mockPreparedStatement2.setInt(1, 1);
		expect(mockPreparedStatement2.execute()).andThrow(new SQLException("Fake errors to force replaying")).once();
		
		replay(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		Connection con = pool.getConnection();
		count=0;


		
		PreparedStatement ps = con.prepareStatement("whatever");
		ps.setInt(1, 1);
		try{
			ps.execute();
			fail("Should have thrown exception");
		} catch (SQLException e){
			// expected exception
		}
		
		
		verify(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		mockDriver.disable();
	}
	
}