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

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jsr166y.TransferQueue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Test for connection thread tester
 * @author wwadge
 *
 */
public class TestConnectionMaxAgeTester {
	/** Mock handle. */
	private static BoneCP mockPool;
	/** Mock handle. */
	private static ConnectionPartition mockConnectionPartition;
	/** Mock handle. */
	private static ScheduledExecutorService mockExecutor;
	/** Test class handle. */
	private static ConnectionMaxAgeThread testClass;
	/** Mock handle. */
	private static BoneCPConfig config;
	/** Mock handle. */
	private static Logger mockLogger;

	/** Mock setup.
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void setup() throws ClassNotFoundException{
		mockPool = createNiceMock(BoneCP.class);
		mockConnectionPartition = createNiceMock(ConnectionPartition.class);
		mockExecutor = createNiceMock(ScheduledExecutorService.class);
		
		mockLogger = createNiceMock(Logger.class);
		
		makeThreadSafe(mockLogger, true);
		config = new BoneCPConfig();
		config.setMaxConnectionAge(1);
		
		testClass = new ConnectionMaxAgeThread(mockConnectionPartition, mockExecutor, mockPool, 5000);
	}

	/**
	 * Reset all mocks.
	 */
	@Before
	public void resetMocks(){
		reset(mockPool, mockConnectionPartition, mockExecutor, mockLogger);
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
		verify(mockConnection);
		
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
	@SuppressWarnings({ "unchecked",  })
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
	public void testCloseConnectionWithExceptionCoverage() throws SQLException{
		ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();
		ConnectionMaxAgeThread.logger = null; // make it break.
		mockConnection.internalClose();
		expectLastCall().andThrow(new SQLException());
		
		replay(mockConnection, mockPool);
		try{
			testClass.closeConnection(mockConnection);
		} catch (Exception e){
			// do nothing
		}
		verify(mockConnection, mockPool);
	}
}