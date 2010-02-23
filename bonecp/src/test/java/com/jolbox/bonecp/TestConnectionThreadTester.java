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
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Test for connection thread tester
 * @author wwadge
 *
 */
public class TestConnectionThreadTester {
	/** Mock handle. */
	private static BoneCP mockPool;
	/** Mock handle. */
	private static ConnectionPartition mockConnectionPartition;
	/** Mock handle. */
	private static ScheduledExecutorService mockExecutor;
	/** Test class handle. */
	private ConnectionTesterThread testClass;
	/** Mock handle. */
	private static ConnectionHandle mockConnection;
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
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockLogger = createNiceMock(Logger.class);
		makeThreadSafe(mockLogger, true);
		config = new BoneCPConfig();
		config.setIdleMaxAge(100);
		config.setIdleConnectionTestPeriod(100);
		
	}

	/**
	 * Reset all mocks.
	 */
	@Before
	public void resetMocks(){
		reset(mockPool, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
	}
	
	/** Tests that a connection that is marked broken is closed internally and that the partition is marked as being 
	 * able to create new connections. 
	 * @throws SQLException */
	@Test
	public void testConnectionMarkedBroken() throws SQLException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		expect(mockPool.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnection.isPossiblyBroken()).andReturn(true);
		
		// connection should be closed
		mockConnection.internalClose();
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	
	/** Tests that a connection that has been idle for more than the set time is closed off. 
	 * @throws SQLException */
	@Test
	public void testIdleConnectionIsKilled() throws SQLException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(2);
		fakeFreeConnections.add(mockConnection);
		fakeFreeConnections.add(mockConnection);
		
		expect(mockPool.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(0).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		
		// connection should be closed
		mockConnection.internalClose();
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();

		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	/** Tests that a connection gets to receive a keep-alive. 
	 * @throws SQLException 
	 * @throws InterruptedException */
	@Test
	public void testIdleConnectionIsSentKeepAlive() throws SQLException, InterruptedException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockPool.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		mockPool.putConnectionBackInPartition((ConnectionHandle)anyObject());
		
		// connection should be closed
		mockConnection.setConnectionLastReset(anyLong());

		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}

	/** Tests that an active connection that fails the connection is alive test will get closed. 
	 * @throws SQLException 
	 * @throws InterruptedException */
	@Test
	public void testIdleConnectionFailedKeepAlive() throws SQLException, InterruptedException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockPool.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(false).anyTimes();
		
		// connection should be closed
		mockConnection.internalClose();
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();

		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection);
	}


	/** Tests fake exceptions, connection should be shutdown if the scheduler was marked as going down. Mostly for code coverage. 
	 * @throws SQLException 
	 * @throws InterruptedException */
	@Test
	public void testInterruptedException() throws SQLException, InterruptedException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockPool.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(true);
		mockPool.putConnectionBackInPartition((ConnectionHandle)anyObject());
		expectLastCall().andThrow(new InterruptedException());
		// connection should be closed
		mockConnection.internalClose();
		mockPool.postDestroyConnection(mockConnection);
		expectLastCall().once();
		
		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool);
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
	 * @throws IllegalArgumentException */
	@Test
	public void testExceptionSpurious() throws SQLException, InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockPool.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(false);
		mockPool.putConnectionBackInPartition((ConnectionHandle)anyObject());
		expectLastCall().andThrow(new InterruptedException());
		mockLogger.error((String)anyObject(), (Exception)anyObject());
		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor, mockLogger);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool);
		Field loggerField = this.testClass.getClass().getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(this.testClass, mockLogger);
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
	 * @throws IllegalArgumentException */
	@Test
	public void testExceptionOnCloseConnection() throws SQLException, InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockPool.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockPool.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(false).anyTimes();
		
		// connection should be closed
		mockConnection.internalClose();
		expectLastCall().andThrow(new SQLException());

		
		replay(mockPool, mockConnection, mockConnectionPartition, mockExecutor, mockLogger);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockPool);
		Field loggerField = this.testClass.getClass().getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(this.testClass, mockLogger);
		this.testClass.run();
		verify(mockPool, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
	}
}
