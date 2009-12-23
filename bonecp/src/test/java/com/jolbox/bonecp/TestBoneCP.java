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

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;
import org.hsqldb.jdbc.jdbcResultSet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.ConnectionPartition;

/**
 * @author wwadge
 *
 */
public class TestBoneCP {
	/** Class under test. */
	private static BoneCP testClass;
	/** Mock handle. */
	private static BoneCPConfig mockConfig;
	/** Mock handle. */
	private static ConnectionPartition mockPartition;
	/** Mock handle. */
	private static ScheduledExecutorService mockKeepAliveScheduler;
	/** Mock handle. */
	private static ExecutorService mockConnectionsScheduler;
	/** Mock handle. */
	private static ArrayBlockingQueue<ConnectionHandle> mockConnectionHandles;
	/** Mock handle. */
	private static ConnectionHandle mockConnection;
	/** Mock handle. */
	private static Lock mockLock;
	/** Mock handle. */
	private static Logger mockLogger;
	/** Mock handle. */
	private static DatabaseMetaData mockDatabaseMetadata;
	/** Mock handle. */
	private static jdbcResultSet mockResultSet;

	/** Mock setups.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setup() throws SQLException, ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Class.forName("org.hsqldb.jdbcDriver");
		mockConfig = createNiceMock(BoneCPConfig.class);
		expect(mockConfig.getPartitionCount()).andReturn(2).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getIdleConnectionTestPeriod()).andReturn(10000L).anyTimes();
		expect(mockConfig.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		expect(mockConfig.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn(CommonTestUtils.url).anyTimes();
		expect(mockConfig.getReleaseHelperThreads()).andReturn(1).once().andReturn(0).anyTimes();
		replay(mockConfig);
		
		// once for no release threads, once with release threads....
		testClass = new BoneCP(mockConfig);
		testClass = new BoneCP(mockConfig);
		
		Field field = testClass.getClass().getDeclaredField("partitions");
		field.setAccessible(true);
		ConnectionPartition[] partitions = (ConnectionPartition[]) field.get(testClass);
		// if all ok 
		assertEquals(2, partitions.length);
		// switch to our mock version now
		mockPartition = createNiceMock(ConnectionPartition.class);
		Array.set(field.get(testClass), 0, mockPartition);
		Array.set(field.get(testClass), 1, mockPartition);
		
		mockKeepAliveScheduler = createNiceMock(ScheduledExecutorService.class); 
		field = testClass.getClass().getDeclaredField("keepAliveScheduler");
		field.setAccessible(true);
		field.set(testClass, mockKeepAliveScheduler);

		field = testClass.getClass().getDeclaredField("connectionsScheduler");
		field.setAccessible(true);
		mockConnectionsScheduler = createNiceMock(ExecutorService.class);
		field.set(testClass, mockConnectionsScheduler);
		
		mockConnectionHandles = createNiceMock(ArrayBlockingQueue.class);
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockLock = createNiceMock(Lock.class);
		mockLogger = createNiceMock(Logger.class);
		makeThreadSafe(mockLogger, true);
		field = testClass.getClass().getDeclaredField("logger");
		field.setAccessible(true);
		field.set(testClass, mockLogger);
		mockDatabaseMetadata = createNiceMock(DatabaseMetaData.class);
		mockResultSet = createNiceMock(jdbcResultSet.class);
			
		mockLogger.error(anyObject());
		expectLastCall().anyTimes();
	}



	
	/**
	 * Reset the mocks.
	 */
	@Before
	public void before(){
		reset(mockConfig, mockKeepAliveScheduler, mockConnectionsScheduler, mockPartition, 
				mockConnectionHandles, mockConnection, mockLock);
	}
	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#shutdown()}.
	 */
	@Test
	public void testShutdown() {
		testShutdownClose(true);
	}



	/**
	 * Tests shutdown/close method.
	 * @param doShutdown call shutdown or call close
	 */
	private void testShutdownClose(boolean doShutdown) {
		expect(mockKeepAliveScheduler.shutdownNow()).andReturn(null).once();
		expect(mockConnectionsScheduler.shutdownNow()).andReturn(null).once();
		expect(mockConnectionHandles.poll()).andReturn(null).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		replay(mockConnectionsScheduler, mockKeepAliveScheduler, mockPartition, mockConnectionHandles);
		
		if (doShutdown){
			testClass.shutdown();
		} else {
			testClass.close();
		}
		verify(mockConnectionsScheduler, mockKeepAliveScheduler, mockPartition, mockConnectionHandles);
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#close()}.
	 */ 
	@Test
	public void testClose() {
		testShutdownClose(false);
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#terminateAllConnections()}.
	 * @throws SQLException 
	 */
	@Test
	public void testTerminateAllConnections() throws SQLException {
		expect(mockConnectionHandles.poll()).andReturn(mockConnection).times(2).andReturn(null).once();
		mockConnection.internalClose();
		expectLastCall().once().andThrow(new SQLException()).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).anyTimes();
		replay(mockConnectionsScheduler, mockKeepAliveScheduler, mockPartition, mockConnectionHandles, mockConnection);
		
		// test.
		testClass.terminateAllConnections();
		verify(mockConnectionsScheduler, mockKeepAliveScheduler, mockPartition, mockConnectionHandles, mockConnection);

	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#getConnection()}.
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	@Test
	public void testGetConnection() throws SQLException, InterruptedException, IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		// Test 1: Get connection - normal state
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(true).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.poll()).andReturn(mockConnection).once();
		mockConnection.setOriginatingPartition(mockPartition);
		expectLastCall().once();
		mockConnection.renewConnection();
		expectLastCall().once();

		replay(mockPartition, mockConnectionHandles, mockConnection);
		assertEquals(mockConnection, testClass.getConnection());
		verify(mockPartition, mockConnectionHandles, mockConnection);
	
		// Test #2: get connection, not finding any available block to wait for one
		reset(mockPartition, mockConnectionHandles, mockConnection);
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(true).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.poll()).andReturn(null).once();
		expect(mockConnectionHandles.take()).andReturn(mockConnection).once();
		
		mockConnection.setOriginatingPartition(mockPartition);
		expectLastCall().once();
		mockConnection.renewConnection();
		expectLastCall().once();

		replay(mockPartition, mockConnectionHandles, mockConnection);
		assertEquals(mockConnection, testClass.getConnection());
		verify(mockPartition, mockConnectionHandles, mockConnection);
	

		// Test #3: Like test #2 but simulate an interrupted exception
		Field field = testClass.getClass().getDeclaredField("connectionStarvationTriggered");
		field.setAccessible(true);
		field.setBoolean(testClass, true);
		reset(mockPartition, mockConnectionHandles, mockConnection);
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(true).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.take()).andThrow(new InterruptedException()).once();
		
		replay(mockPartition, mockConnectionHandles, mockConnection);
		try{
			testClass.getConnection();
			fail("Should have throw an SQL Exception");
		} catch (SQLException e){
			// do nothing
		}
		verify(mockPartition, mockConnectionHandles, mockConnection);

		
		// Test #4: Like test #2 but simulate an interrupted exception in another take()
		 field = testClass.getClass().getDeclaredField("connectionStarvationTriggered");
		field.setAccessible(true);
		field.setBoolean(testClass, false);
		reset(mockPartition, mockConnectionHandles, mockConnection);
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(true).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.poll()).andReturn(null).once();
		expect(mockConnectionHandles.take()).andThrow(new InterruptedException()).once();
		
		replay(mockPartition, mockConnectionHandles, mockConnection);
		try{
			testClass.getConnection();
			fail("Should have throw an SQL Exception");
		} catch (SQLException e){
			// do nothing
		}
		verify(mockPartition, mockConnectionHandles, mockConnection);

		// Test #5: Connection queues are starved of free connections. Should block and wait on one without spin-locking.
		field = testClass.getClass().getDeclaredField("connectionStarvationTriggered");
		field.setAccessible(true);
		field.setBoolean(testClass, true);
		reset(mockPartition, mockConnectionHandles, mockConnection);
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(true).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.take()).andReturn(mockConnection).once();

		mockConnection.setOriginatingPartition(mockPartition);
		expectLastCall().once();
		mockConnection.renewConnection();
		expectLastCall().once();

		replay(mockPartition, mockConnectionHandles, mockConnection);
		assertEquals(mockConnection, testClass.getConnection());
		verify(mockPartition, mockConnectionHandles, mockConnection);

		// Test #6: If we hit our limit, we should signal for more connections to be created on the fly
		reset(mockPartition, mockConnectionHandles, mockConnection);
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(false).anyTimes();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockPartition.getMaxConnections()).andReturn(1).anyTimes();

		mockPartition.almostFullSignal();
		expectLastCall().once();

		expect(mockConnectionHandles.poll()).andReturn(mockConnection).once();
		mockConnection.setOriginatingPartition(mockPartition);
		expectLastCall().once();
		mockConnection.renewConnection();
		expectLastCall().once();

		replay(mockPartition, mockConnectionHandles, mockConnection);
		testClass.getConnection();
		verify(mockPartition, mockConnectionHandles, mockConnection);

		
		// Test #7: Like test 6, except we fake an unchecked exception to make sure our locks are released.
		field = testClass.getClass().getDeclaredField("connectionsObtainedLock");
		field.setAccessible(true);
		field.set(testClass, mockLock);
		
		reset(mockPartition, mockConnectionHandles, mockConnection);
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(false).anyTimes();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockPartition.getMaxConnections()).andReturn(0).anyTimes(); // cause a division by zero error
		mockLock.lock();
		expectLastCall().once();
		mockLock.unlock();
		expectLastCall().once();
		replay(mockPartition, mockConnectionHandles, mockConnection, mockLock);
		try{
			testClass.getConnection();
			fail("Should have thrown an exception");
		} catch (Throwable t){
			// do nothing
		}
		verify(mockPartition, mockConnectionHandles, mockConnection, mockLock);

		

	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#releaseConnection(java.sql.Connection)}.
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testReleaseConnection() throws SQLException, IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException {
		// Test #1: Test releasing connections directly (without helper threads)
		Field field = testClass.getClass().getDeclaredField("releaseHelperThreadsConfigured");
		field.setAccessible(true);
		field.setBoolean(testClass, false);

		expect(mockConnection.isPossiblyBroken()).andReturn(false).once();
		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).anyTimes();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.offer(mockConnection)).andReturn(false).anyTimes();
		mockConnectionHandles.put(mockConnection);
		expectLastCall().once();

		// should reset last connection use
		mockConnection.setConnectionLastUsed(anyLong());
		expectLastCall().once();
		replay(mockConnection, mockPartition, mockConnectionHandles);
		testClass.releaseConnection(mockConnection);
		verify(mockConnection, mockPartition, mockConnectionHandles);

		reset(mockConnection, mockPartition, mockConnectionHandles);

		// Test #2: Same test as 1 but throw an exception instead
		field = testClass.getClass().getDeclaredField("releaseHelperThreadsConfigured");
		field.setAccessible(true);
		field.setBoolean(testClass, false);

		expect(mockConnection.isPossiblyBroken()).andReturn(false).once();
		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).anyTimes();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.offer(mockConnection)).andReturn(false).anyTimes();
		mockConnectionHandles.put(mockConnection);
		expectLastCall().andThrow(new InterruptedException()).once();
 
		// should reset last connection use
		mockConnection.setConnectionLastUsed(anyLong());
		expectLastCall().once();
		replay(mockConnection, mockPartition, mockConnectionHandles);
		try{
			testClass.releaseConnection(mockConnection);
			fail("Should have thrown an exception");
		} catch (SQLException e){ 
			// do nothing
		}
		verify(mockConnection, mockPartition, mockConnectionHandles);

		
		reset(mockConnection, mockPartition, mockConnectionHandles);

		// Test #2: Test with releaser helper threads configured
		field = testClass.getClass().getDeclaredField("releaseHelperThreadsConfigured");
		field.setAccessible(true);
		field.setBoolean(testClass, true);

		ArrayBlockingQueue<ConnectionHandle> mockPendingRelease = createNiceMock(ArrayBlockingQueue.class);
		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).anyTimes();
		expect(mockPartition.getConnectionsPendingRelease()).andReturn(mockPendingRelease).anyTimes();
		mockPendingRelease.put(mockConnection);
		expectLastCall().once();
		
		replay(mockConnection, mockPartition, mockConnectionHandles, mockPendingRelease);
		testClass.releaseConnection(mockConnection);
		verify(mockConnection, mockPartition, mockConnectionHandles, mockPendingRelease);

	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#internalReleaseConnection(ConnectionHandle)}.
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	@Test
	public void testInternalReleaseConnection() throws InterruptedException, SQLException {
		// Test #1: Test normal case where connection is considered to be not broken
		// should reset last connection use
		mockConnection.setConnectionLastUsed(anyLong());
		expectLastCall().once();
		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).anyTimes();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.offer(mockConnection)).andReturn(false).anyTimes();
		mockConnectionHandles.put(mockConnection);
		expectLastCall().once();
		
		replay(mockConnection,mockPartition, mockConnectionHandles);
		testClass.internalReleaseConnection(mockConnection);
		verify(mockConnection,mockPartition, mockConnectionHandles);
		
		// Test #2: Test case where connection is broken
		reset(mockConnection,mockPartition, mockConnectionHandles);
		expect(mockConnection.isPossiblyBroken()).andReturn(true).once();
		expect(mockConfig.getConnectionTestStatement()).andReturn(null).once();
		// make it fail to return false
		expect(mockConnection.getMetaData()).andThrow(new SQLException()).once();

		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).anyTimes();
		// we're about to destroy this connection, so we can create new ones.
		mockPartition.setUnableToCreateMoreTransactions(false);
		expectLastCall().once();

		// break out from the next method, we're not interested in it
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(true).once();
		replay(mockPartition, mockConnection);
		testClass.internalReleaseConnection(mockConnection);
		verify(mockPartition, mockConnection);
		
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#releaseInAnyFreePartition(com.jolbox.bonecp.ConnectionHandle, com.jolbox.bonecp.ConnectionPartition)}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testReleaseInAnyFreePartition() throws InterruptedException {
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		// Test 1: Active partition is full up
		expect(mockConnectionHandles.offer(mockConnection)).andReturn(false).once().andReturn(true).once();
		replay(mockPartition, mockConnectionHandles);
		testClass.releaseInAnyFreePartition(mockConnection, mockPartition);
		verify(mockPartition, mockConnectionHandles);
		
		// Test #2: Same test where all partitions are full
		reset(mockPartition, mockConnectionHandles);
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.offer(mockConnection)).andReturn(false).anyTimes();
		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).once();
		
		mockConnectionHandles.put(mockConnection);
		expectLastCall().once();
		replay(mockPartition, mockConnectionHandles, mockConnection);
		testClass.releaseInAnyFreePartition(mockConnection, mockPartition);
		verify(mockPartition, mockConnectionHandles, mockConnection);
		
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#isConnectionHandleAlive(com.jolbox.bonecp.ConnectionHandle)}.
	 * @throws SQLException 
	 */
	@Test
	public void testIsConnectionHandleAlive() throws SQLException {
		// Test 1: Normal case (+ without connection test statement)
		expect(mockConfig.getConnectionTestStatement()).andReturn(null).once();
		expect(mockConnection.getMetaData()).andReturn(mockDatabaseMetadata).once();
		expect(mockDatabaseMetadata.getTables((String)anyObject(), (String)anyObject(), (String)anyObject(), (String[])anyObject())).andReturn(mockResultSet).once();
		mockResultSet.close();
		expectLastCall().once();
		
		replay(mockConfig, mockConnection, mockDatabaseMetadata, mockResultSet);
		assertTrue(testClass.isConnectionHandleAlive(mockConnection));
		verify(mockConfig, mockConnection, mockResultSet,mockDatabaseMetadata);
		
		// Test 2: Same test as 1 but triggers an exception
		reset(mockConfig, mockConnection, mockDatabaseMetadata, mockResultSet);
		expect(mockConfig.getConnectionTestStatement()).andReturn(null).once();
		expect(mockConnection.getMetaData()).andThrow(new SQLException()).once();
		
		replay(mockConfig, mockConnection, mockDatabaseMetadata, mockResultSet);
		assertFalse(testClass.isConnectionHandleAlive(mockConnection));
		verify(mockConfig, mockConnection, mockResultSet,mockDatabaseMetadata);
		

		// Test 3: Normal case (+ with connection test statement)
		reset(mockConfig, mockConnection, mockDatabaseMetadata, mockResultSet);

		Statement mockStatement = createNiceMock(Statement.class);
		expect(mockConfig.getConnectionTestStatement()).andReturn("whatever").once();
		expect(mockConnection.createStatement()).andReturn(mockStatement).once();
		expect(mockStatement.executeQuery((String)anyObject())).andReturn(mockResultSet).once();
		mockResultSet.close();
		expectLastCall().once();
		
		replay(mockConfig, mockConnection, mockDatabaseMetadata,mockStatement, mockResultSet);
		assertTrue(testClass.isConnectionHandleAlive(mockConnection));
		verify(mockConfig, mockConnection, mockResultSet,mockDatabaseMetadata, mockStatement);

		// Test 4: Same like test 3 but triggers exception
		reset(mockConfig, mockConnection, mockDatabaseMetadata, mockResultSet, mockStatement);

		expect(mockConfig.getConnectionTestStatement()).andReturn("whatever").once();
		expect(mockConnection.createStatement()).andReturn(mockStatement).once();
		expect(mockStatement.executeQuery((String)anyObject())).andThrow(new RuntimeException()).once();
		mockStatement.close();
		expectLastCall().once();
		
		replay(mockConfig, mockConnection, mockDatabaseMetadata,mockStatement, mockResultSet);
//		assertFalse(testClass.isConnectionHandleAlive(mockConnection));
		try{
			testClass.isConnectionHandleAlive(mockConnection);
			fail("Should have thrown an exception");
		} catch (RuntimeException e){
			// do nothing 
		}
		verify(mockConfig, mockConnection, mockResultSet,mockDatabaseMetadata, mockStatement);

		// Test 5: Same like test 4 but triggers exception in finally block
		reset(mockConfig, mockConnection, mockDatabaseMetadata, mockResultSet, mockStatement);

		expect(mockConfig.getConnectionTestStatement()).andReturn("whatever").once();
		expect(mockConnection.createStatement()).andReturn(mockStatement).once();
		expect(mockStatement.executeQuery((String)anyObject())).andThrow(new RuntimeException()).once();
		mockStatement.close();
		expectLastCall().andThrow(new SQLException()).once();
		
		replay(mockConfig, mockConnection, mockDatabaseMetadata,mockStatement, mockResultSet);
		try{
			testClass.isConnectionHandleAlive(mockConnection);
			fail("Should have thrown an exception");
		} catch (RuntimeException e){
			// do nothing
		}
		verify(mockConfig, mockConnection, mockResultSet,mockDatabaseMetadata, mockStatement);

	}

	/**
	 * Test method for maybeSignalForMoreConnections(com.jolbox.bonecp.ConnectionPartition)}.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */
	@Test
	public void testMaybeSignalForMoreConnections() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(false).once();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.size()).andReturn(1).anyTimes();
		expect(mockPartition.getMaxConnections()).andReturn(10).anyTimes();
		mockPartition.lockAlmostFullLock();
		expectLastCall().once();
		mockPartition.almostFullSignal();
		expectLastCall().once();
		mockPartition.unlockAlmostFullLock();
		expectLastCall().once();
		replay(mockPartition, mockConnectionHandles);
		Method method = testClass.getClass().getDeclaredMethod("maybeSignalForMoreConnections", ConnectionPartition.class);
		method.setAccessible(true);
		method.invoke(testClass, new Object[]{mockPartition});
		verify(mockPartition, mockConnectionHandles);
		
		// Test 2, same test but fake an exception
		reset(mockPartition, mockConnectionHandles);
		expect(mockPartition.isUnableToCreateMoreTransactions()).andReturn(false).anyTimes();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.size()).andReturn(1).anyTimes();
		expect(mockPartition.getMaxConnections()).andReturn(10).anyTimes();
		mockPartition.lockAlmostFullLock();
		expectLastCall().once();
		mockPartition.almostFullSignal();
		expectLastCall().andThrow(new RuntimeException()).once();
		mockPartition.unlockAlmostFullLock();
		expectLastCall().once(); 
		replay(mockPartition, mockConnectionHandles);
		try{
			method.invoke(testClass, new Object[]{mockPartition});
			fail("Should have thrown an exception");
		} catch (Throwable t){
			// do nothing
		}
		verify(mockPartition, mockConnectionHandles);
		
	}
	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#getTotalLeased()}.
	 */
	@Test
	public void testGetTotalLeased() {
		expect(mockPartition.getCreatedConnections()).andReturn(5).anyTimes();
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.size()).andReturn(3).anyTimes();
		replay(mockPartition, mockConnectionHandles);
		assertEquals(4, testClass.getTotalLeased());
		verify(mockPartition, mockConnectionHandles);
		
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#getTotalFree()}.
	 */
	@Test
	public void testGetTotalFree() {
		expect(mockPartition.getFreeConnections()).andReturn(mockConnectionHandles).anyTimes();
		expect(mockConnectionHandles.size()).andReturn(3).anyTimes();
		replay(mockPartition, mockConnectionHandles);
		assertEquals(6, testClass.getTotalFree());
		verify(mockPartition, mockConnectionHandles);

	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#getTotalCreatedConnections()}.
	 */
	@Test
	public void testGetTotalCreatedConnections() {
		expect(mockPartition.getCreatedConnections()).andReturn(5).anyTimes();
		replay(mockPartition);
		assertEquals(10, testClass.getTotalCreatedConnections());
		verify(mockPartition);

	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#getConfig()}.
	 */
	@Test
	public void testGetConfig() {
		assertEquals(mockConfig, testClass.getConfig());
	}

	/**
	 * Test method for {@link com.jolbox.bonecp.BoneCP#setReleaseHelper(java.util.concurrent.ExecutorService)}.
	 */
	@Test
	public void testSetReleaseHelper() {
		ExecutorService mockReleaseHelper = createNiceMock(ExecutorService.class);
		testClass.setReleaseHelper(mockReleaseHelper);
		assertEquals(mockReleaseHelper, testClass.getReleaseHelper());
	}

}
