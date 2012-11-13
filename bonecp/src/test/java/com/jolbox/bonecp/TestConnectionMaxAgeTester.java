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

package com.jolbox.bonecp;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;


import java.util.concurrent.TransferQueue;



/**
 * Test for connection thread tester
 * @author wwadge
 *
 */
public class TestConnectionMaxAgeTester {
	/** Mock handle. */
	private BoneCP mockPool;
	/** Mock handle. */
	private ConnectionPartition mockConnectionPartition;
	/** Mock handle. */
	private ScheduledExecutorService mockExecutor;
	/** Test class handle. */
	private ConnectionMaxAgeThread testClass;
	/** Mock handle. */
	private BoneCPConfig config;
	/** Mock handle. */
	private Logger mockLogger;

	/**
	 * Mock setup.
	 * @throws ClassNotFoundException
	 */
	@Before
	public void resetMocks() throws ClassNotFoundException{
		mockPool = createNiceMock(BoneCP.class);
		mockConnectionPartition = createNiceMock(ConnectionPartition.class);
		mockExecutor = createNiceMock(ScheduledExecutorService.class);
		
		mockLogger = createNiceMock(Logger.class);
		
		makeThreadSafe(mockLogger, true);
		config = new BoneCPConfig();
		config.setMaxConnectionAgeInSeconds(1);
		
		testClass = new ConnectionMaxAgeThread(mockConnectionPartition, mockExecutor, mockPool, 5000, false);
	}
	
	/**
	 * Tests that a partition with expired connections should those connections killed off.
	 * @throws SQLException 
	 */
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testConnectionExpired() throws SQLException{
		 
		TransferQueue<ConnectionHandle> mockQueue = createNiceMock(TransferQueue.class);
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1);
		expect(mockConnectionPartition.getFreeConnections()).andReturn(mockQueue).anyTimes();
		ConnectionHandle mockConnectionExpired = createNiceMock(ConnectionHandle.class);
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		expect(mockQueue.poll()).andReturn(mockConnectionExpired).once();
			
		expect(mockConnectionExpired.isExpired(anyLong())).andReturn(true).once();

		expect(mockExecutor.isShutdown()).andReturn(false).once();
		
		mockConnectionExpired.internalClose();
		expectLastCall().once();
		
		mockPool.postDestroyConnection(mockConnectionExpired);
		expectLastCall().once();
		
		
		expect(mockExecutor.schedule((Callable)anyObject(), anyLong(), (TimeUnit)anyObject())).andReturn(null).once();
		replay(mockQueue, mockExecutor, mockConnectionPartition, mockConnection, mockPool, mockConnectionExpired);
		testClass.run();
		verify(mockConnectionExpired);
	}


	/**
	 * Tests that a partition with expired connections should those connections killed off.
	 * @throws SQLException 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testConnectionNotExpiredLifoMode() throws SQLException{
		 
		LIFOQueue<ConnectionHandle> mockQueue = createNiceMock(LIFOQueue.class);
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1);
		expect(mockConnectionPartition.getFreeConnections()).andReturn(mockQueue).anyTimes();
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		expect(mockQueue.poll()).andReturn(mockConnection).once();
			
		expect(mockConnection.isExpired(anyLong())).andReturn(false).once();

		expect(mockExecutor.isShutdown()).andReturn(false).once();
		

		expect(mockConnection.getOriginatingPartition()).andReturn(mockConnectionPartition).anyTimes();
		expect(mockQueue.offerLast(mockConnection)).andReturn(false).anyTimes();
		mockConnection.internalClose();
		
		replay(mockQueue, mockExecutor, mockConnectionPartition, mockConnection, mockPool);
		ConnectionMaxAgeThread testClass2 = new ConnectionMaxAgeThread(mockConnectionPartition, mockExecutor, mockPool, 5000, true);
		testClass2.run();

		verify(mockConnection, mockPool);
		
	}
	
	/**
	 * Tests that a partition with expired connections should those connections killed off.
	 * @throws SQLException 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testConnectionNotExpired() throws SQLException{
		 
		TransferQueue<ConnectionHandle> mockQueue = createNiceMock(TransferQueue.class);
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1);
		expect(mockConnectionPartition.getFreeConnections()).andReturn(mockQueue).anyTimes();
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		expect(mockQueue.poll()).andReturn(mockConnection).once();
			
		expect(mockConnection.isExpired(anyLong())).andReturn(false).once();

		expect(mockExecutor.isShutdown()).andReturn(false).once();
		
		mockPool.putConnectionBackInPartition(mockConnection);
		expectLastCall().once();
		
		
		replay(mockQueue, mockExecutor, mockConnectionPartition, mockConnection, mockPool);
		testClass.run();
		verify(mockConnection, mockPool);
		
	}
	
	/**
	 * @throws SQLException
	 */
	@Test
	@SuppressWarnings({ "unchecked" })
	public void testExceptionsCase() throws SQLException{
		 
		TransferQueue<ConnectionHandle> mockQueue = createNiceMock(TransferQueue.class);
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(2);
		expect(mockConnectionPartition.getFreeConnections()).andReturn(mockQueue).anyTimes();
		ConnectionHandle mockConnectionException = createNiceMock(ConnectionHandle.class);
		expect(mockQueue.poll()).andReturn(mockConnectionException).times(2);
		expect(mockConnectionException.isExpired(anyLong())).andThrow(new RuntimeException()).anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(false).once().andReturn(true).once();
		
		replay(mockQueue,mockConnectionException, mockExecutor, mockConnectionPartition, mockPool);
		testClass.run();
		verify(mockExecutor, mockConnectionException);
		
	}
	
	/**
	 * @throws SQLException
	 */
	@Test
	@SuppressWarnings( "unchecked")
	public void testExceptionsCaseWherePutInPartitionFails() throws SQLException{
		 
		TransferQueue<ConnectionHandle> mockQueue = createNiceMock(TransferQueue.class);
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1);
		expect(mockConnectionPartition.getFreeConnections()).andReturn(mockQueue).anyTimes();
		ConnectionHandle mockConnectionException = createNiceMock(ConnectionHandle.class);
		expect(mockQueue.poll()).andReturn(mockConnectionException).times(1);
		expect(mockConnectionException.isExpired(anyLong())).andReturn(false).anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(false).anyTimes();
		mockPool.putConnectionBackInPartition(mockConnectionException);
		expectLastCall().andThrow(new SQLException()).once();
		
		// we should be able to reschedule
		expect(mockExecutor.schedule((Runnable)anyObject(), anyLong(), (TimeUnit)anyObject())).andReturn(null).once();
		
		replay(mockQueue,mockConnectionException, mockExecutor, mockConnectionPartition, mockPool);
		testClass.run();
		verify(mockExecutor, mockConnectionException);
	}
	
	/**
	 * @throws SQLException
	 */
	@Test
	public void testCloseConnectionNormalCase() throws SQLException{
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();
		
		mockConnection.internalClose();
		expectLastCall().once();
		
		replay(mockConnection, mockPool);
		testClass.closeConnection(mockConnection);
		verify(mockConnection, mockPool);
	}
	
	/**
	 * @throws SQLException
	 */
	@Test
	public void testCloseConnectionWithException() throws SQLException{
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();
		
		mockConnection.internalClose();
		expectLastCall().andThrow(new SQLException());
		
		replay(mockConnection, mockPool);
		testClass.closeConnection(mockConnection);
		verify(mockConnection, mockPool);
	}
	
	/**
	 * @throws SQLException
	 */
	@Test
	public void testCloseConnectionWithExceptionCoverage() throws Exception{
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();
    // set logger to null so that exception will be thrown in catch clause
    Field field = ConnectionMaxAgeThread.class.getDeclaredField("logger");
    TestUtils.setFinalStatic(field, null);
		mockConnection.internalClose();
		expectLastCall().andThrow(new SQLException());
		
		replay(mockConnection, mockPool);
		try{
			testClass.closeConnection(mockConnection);
      fail("Expecting NPE because logger was set to null");
		} catch (Exception e){
			// do nothing
		}
		verify(mockConnection, mockPool);
	}
}