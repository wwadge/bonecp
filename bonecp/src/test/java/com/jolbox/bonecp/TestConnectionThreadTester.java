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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.makeThreadSafe;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Test for connection thread tester
 * @author wwadge
 *
 */
public class TestConnectionThreadTester {
	/** Mock handle. */
	private BoneCP mockPool;
	/** Mock handle. */
	private ConnectionPartition mockConnectionPartition;
	/** Mock handle. */
	private ScheduledExecutorService mockExecutor;
	/** Test class handle. */
	private ConnectionTesterThread testClass;
	/** Mock handle. */
	private ConnectionHandle mockConnection;
	/** Mock handle. */
	private BoneCPConfig config;
	/** Mock handle. */
	private Logger mockLogger;

	/** Mock setup.
	 * @throws ClassNotFoundException
	 */
	@Before
	public void resetMocks() throws ClassNotFoundException{
		mockPool = createNiceMock(BoneCP.class);
		mockConnectionPartition = createNiceMock(ConnectionPartition.class);
		mockExecutor = createNiceMock(ScheduledExecutorService.class);
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockLogger = createNiceMock(Logger.class);
		
		makeThreadSafe(mockLogger, true);
		config = new BoneCPConfig();
		config.setIdleMaxAgeInMinutes(100);
		config.setIdleConnectionTestPeriodInMinutes(100);
	}
	
	/** Tests that a connection that is marked broken is closed internally and that the partition is marked as being 
	 * able to create new connections. 
	 * @throws SQLException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testConnectionMarkedBroken() throws SQLException, CloneNotSupportedException {
		BlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		
		BoneCPConfig localconfig = config.clone();
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1).anyTimes();

		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
 		expect(mockConnection.isPossiblyBroken()).andReturn(true);
		
		// connection should be closed
		mockConnection.internalClose();
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAgeInMinutes(), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	
	/** Tests that a connection that has been idle for more than the set time is closed off. 
	 * @throws SQLException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testIdleConnectionIsKilled() throws SQLException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		fakeFreeConnections.add(mockConnection);
		BoneCPConfig localconfig = config.clone();
		expect(mockPool.getConfig()).andReturn(localconfig.clone()).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(2).anyTimes();
		
//		expect(mockConnectionPartition.getMinConnections()).andReturn(0).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(0L);
		
		// connection should be closed
		mockConnection.internalClose();
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();

		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAge(TimeUnit.MILLISECONDS), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	
	/** Tests that a connection that has been idle for more than the set time is closed off but during
	 * closing, an exception occurs (should update partition counts). 
	 * @throws SQLException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testIdleConnectionIsKilledWithFailure() throws SQLException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		fakeFreeConnections.add(mockConnection);
		BoneCPConfig localconfig = config.clone();
		expect(mockPool.getConfig()).andReturn(localconfig.clone()).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(2).anyTimes();
		
//		expect(mockConnectionPartition.getMinConnections()).andReturn(0).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(0L);
		
		// connection should be closed
		mockConnection.internalClose();
		expectLastCall().andThrow(new RuntimeException());
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();

		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAgeInMinutes(), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	/** Tests that a connection gets to receive a keep-alive. 
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testIdleConnectionIsSentKeepAlive() throws SQLException, InterruptedException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		BoneCPConfig localconfig = config.clone();
		localconfig.setIdleConnectionTestPeriodInMinutes(1);
		localconfig.setIdleMaxAgeInMinutes(0);
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
//		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(2).anyTimes();
		
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		mockPool.putConnectionBackInPartition((ConnectionHandle)anyObject());
		

		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAgeInMinutes(), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	/** Tests that a connection gets to receive a keep-alive. 
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testIdleMaxAge() throws SQLException, InterruptedException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		BoneCPConfig localconfig = config.clone();
		localconfig.setIdleConnectionTestPeriodInMinutes(0);
		localconfig.setIdleMaxAgeInMinutes(1);
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
//		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(2).anyTimes();
		
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(Long.MAX_VALUE);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		mockPool.putConnectionBackInPartition((ConnectionHandle)anyObject());
		

		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAgeInMinutes(), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	/** Tests that a connection gets to receive a keep-alive. 
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws CloneNotSupportedException */
	@SuppressWarnings("unchecked")
	@Test
	public void testIdleMaxAgeLifoMode() throws SQLException, InterruptedException, CloneNotSupportedException {
		BlockingQueue<ConnectionHandle> mockFreeConnections = createNiceMock(LinkedBlockingQueue.class);
		expect(mockFreeConnections.poll()).andReturn(mockConnection).once();
		BoneCPConfig localconfig = config.clone();
		localconfig.setIdleConnectionTestPeriodInMinutes(0);
		localconfig.setIdleMaxAgeInMinutes(1);
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(mockFreeConnections).anyTimes();
		expect(mockConnection.getOriginatingPartition()).andReturn(mockConnectionPartition).once();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(2).anyTimes();
		expect(mockFreeConnections.offer((ConnectionHandle)anyObject())).andReturn(false).anyTimes();
		mockConnection.internalClose();
		expectLastCall().once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(Long.MAX_VALUE);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		//mockPool.putConnectionBackInPartition((ConnectionHandle)anyObject());
		

		replay(mockPool, mockFreeConnections, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAgeInMinutes(), localconfig.getIdleConnectionTestPeriodInMinutes(), true);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}
	/** Tests that an active connection that fails the connection is alive test will get closed. 
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testIdleConnectionFailedKeepAlive() throws SQLException, InterruptedException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		BoneCPConfig localconfig = config.clone();
		localconfig.setIdleConnectionTestPeriodInMinutes(1);
		
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
//		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1).anyTimes();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(System.currentTimeMillis());
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(false).anyTimes();
		
		// connection should be closed
		mockConnection.internalClose();
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();

		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAge(TimeUnit.MILLISECONDS), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}


	/** Tests fake exceptions, Mostly for code coverage. 
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testInterruptedException() throws SQLException, InterruptedException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		BoneCPConfig localconfig = config.clone();
		localconfig.setIdleConnectionTestPeriodInMinutes(1);
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getAvailableConnections()).andThrow(new RuntimeException()).once(); // anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(true);
		
		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAge(TimeUnit.MILLISECONDS), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}


	/** Tests fake exceptions, connection should be shutdown if the scheduler was marked as going down. Same test except just used
	 * to check for a spurious interrupted exception (should be logged). 
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testExceptionSpurious() throws SQLException, InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(10);
		fakeFreeConnections.add(mockConnection);
		BoneCPConfig localconfig = config.clone();
		localconfig.setIdleConnectionTestPeriodInMinutes(1);
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
//		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1).anyTimes();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(System.currentTimeMillis());

		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(false);
		mockPool.putConnectionBackInPartition((ConnectionHandle)anyObject());
		expectLastCall().andThrow(new RuntimeException());
		mockLogger.error((String)anyObject(), (Exception)anyObject());
		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor, mockLogger);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAgeInMinutes(), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
    Field loggerField = this.testClass.getClass().getDeclaredField("logger");
    TestUtils.setFinalStatic(loggerField, mockLogger);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
	}

	
	/** Tests fake exceptions, connection should be shutdown if the scheduler was marked as going down. Same test except just used
	 * to check for a spurious interrupted exception (should be logged). 
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws CloneNotSupportedException */
	@Test
	public void testExceptionOnCloseConnection() throws SQLException, InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, CloneNotSupportedException {
		LinkedBlockingQueue<ConnectionHandle> fakeFreeConnections = new LinkedBlockingQueue<ConnectionHandle>(100);
		fakeFreeConnections.add(mockConnection);
		
		BoneCPConfig localconfig = config.clone();
		localconfig.setIdleConnectionTestPeriodInMinutes(1);
		expect(mockPool.getConfig()).andReturn(localconfig).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
//		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnectionPartition.getAvailableConnections()).andReturn(1).anyTimes();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsedInMs()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(false).anyTimes();
		
		// connection should be closed
		mockConnection.internalClose();
		expectLastCall().andThrow(new SQLException());

		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor, mockLogger);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool, localconfig.getIdleMaxAgeInMinutes(), localconfig.getIdleConnectionTestPeriodInMinutes(), false);
		Field loggerField = this.testClass.getClass().getDeclaredField("logger");
    TestUtils.setFinalStatic(loggerField, mockLogger);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
	}
}