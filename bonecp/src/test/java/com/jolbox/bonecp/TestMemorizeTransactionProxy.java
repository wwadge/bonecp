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

/**
 * @author Wallace
 *
 */
public class TestMemorizeTransactionProxy {
	
	static ConnectionHandle mockConnection;
	static Connection mockConnection2;
	static CallableStatement mockCallableStatement;
	static Statement mockStatement;
	static PreparedStatement mockPreparedStatement;
	static Connection mockConnection3; 
	
	@BeforeClass
	public static void beforeClass(){
		mockConnection = createNiceMock(ConnectionHandle.class);
		// make it return a new connection when asked for again
		mockConnection2 = createNiceMock(Connection.class);
		mockConnection3 = createNiceMock(Connection.class);
		mockCallableStatement = createNiceMock(CallableStatement.class);
		mockStatement = createNiceMock(Statement.class);
		mockPreparedStatement = createNiceMock(PreparedStatement.class);
	}
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
		CommonTestUtils.config.setTransactionRecoveryEnabled(true);
		CommonTestUtils.config.setJdbcUrl("jdbc:mock:driver2");
		CommonTestUtils.config.setReleaseHelperThreads(0);
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

		
		// fake stuff to test for clear
		((ConnectionHandle)con).getReplayLog().add(new ReplayLog(null, null, null));
		((ConnectionHandle)con).recoveryResult.getReplaceTarget().put("test", "test1");
		con.rollback(); // should clear out log
		assertTrue(((ConnectionHandle)con).getReplayLog().isEmpty());
		assertTrue(((ConnectionHandle)con).recoveryResult.getReplaceTarget().isEmpty());

		// fake stuff to test for clear
		((ConnectionHandle)con).getReplayLog().add(new ReplayLog(null, null, null));
		((ConnectionHandle)con).recoveryResult.getReplaceTarget().put("test", "test1");
		con.commit(); // should clear out log
		assertTrue(((ConnectionHandle)con).getReplayLog().isEmpty());
		assertTrue(((ConnectionHandle)con).recoveryResult.getReplaceTarget().isEmpty());

		assertNotNull(((ConnectionHandle)con).getProxyTarget());
		mockDriver.disable();
	}
	
	static int count = 1;

	
	@Test
	public void testReplayTransaction() throws IllegalArgumentException, Throwable{
		
		count = 1;

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
		reset(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);

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
	
	
	@Test
	public void testReplayTransactionWithUserError() throws IllegalArgumentException, Throwable{
		
		count = 1;

		MockJDBCDriver mockDriver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			@Override
			public Connection answer() throws SQLException {
					return TestMemorizeTransactionProxy.this.mockConnection;
			}
		}); 
		
		
		CommonTestUtils.config.setTransactionRecoveryEnabled(true);
		CommonTestUtils.config.setJdbcUrl("jdbc:mock:driver");
		CommonTestUtils.config.setMinConnectionsPerPartition(2);
		CommonTestUtils.config.setMaxConnectionsPerPartition(2);
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		reset(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);

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
			
			@Override
			public Connection answer() throws SQLException {
				if (count == 1){
					return TestMemorizeTransactionProxy.this.mockConnection;
				}  if (count == 0){
					count--;
					return TestMemorizeTransactionProxy.this.mockConnection2;
				} 
					return TestMemorizeTransactionProxy.this.mockConnection3;
			}
		}); 
		
		
		CommonTestUtils.config.setTransactionRecoveryEnabled(true);
		CommonTestUtils.config.setJdbcUrl("jdbc:mock:driver");
		CommonTestUtils.config.setMinConnectionsPerPartition(2);
		CommonTestUtils.config.setMaxConnectionsPerPartition(2);
		CommonTestUtils.config.setAcquireRetryDelay(1);
		CommonTestUtils.config.setAcquireRetryAttempts(2);
//		CommonTestUtils.config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		reset(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);

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
	
	
	@Test
	public void testReplayTransactionWithFailuresOnReplayKeepFailing() throws IllegalArgumentException, Throwable{
		
		count = 1;

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
		CommonTestUtils.config.setAcquireRetryDelay(1);
		CommonTestUtils.config.setAcquireRetryAttempts(1);
//		CommonTestUtils.config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		reset(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);

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
			// expected exception
		}
		
		
		verify(mockConnection, mockPreparedStatement,mockConnection2, mockPreparedStatement2);
		
		mockDriver.disable();
	}
	
	@Test
	public void testReplayTransactionWithFailuresInterruptedException() throws IllegalArgumentException, Throwable{
		
		count = 1;

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
		CommonTestUtils.config.setAcquireRetryDelay(10000);
		CommonTestUtils.config.setAcquireRetryAttempts(2);
//		CommonTestUtils.config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		reset(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);

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
				
				@Override
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
	
	@Test
	public void testReplayTransactionWithFailuresCustomHook() throws IllegalArgumentException, Throwable{
		
		count = 1;

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
		CommonTestUtils.config.setAcquireRetryDelay(1);
		CommonTestUtils.config.setAcquireRetryAttempts(2);
		CommonTestUtils.config.setConnectionHook(new CoverageHook());
		BoneCP pool = new BoneCP(CommonTestUtils.config);
		reset(mockConnection);
		CommonTestUtils.config.setTransactionRecoveryEnabled(false);

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
