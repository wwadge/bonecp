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

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.ConnectionPartition;
import com.jolbox.bonecp.ConnectionTesterThread;

/**
 * Test for connection thread tester
 * @author wwadge
 *
 */
public class TestConnectionThreadTester {
	/** Mock handle. */
	private static BoneCP mockBoneCP;
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
		mockBoneCP = createNiceMock(BoneCP.class);
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
		reset(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
	}
	
	/** Tests that a connection that is marked broken is closed internally and that the partition is marked as being 
	 * able to create new connections. 
	 * @throws SQLException */
	@Test
	public void testConnectionMarkedBroken() throws SQLException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		expect(mockBoneCP.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnection.isPossiblyBroken()).andReturn(true);
		
		// connection should be closed
		mockConnection.internalClose();
		// partition should be set to allow creation of new ones
		mockConnectionPartition.setUnableToCreateMoreTransactions(false);

		replay(mockBoneCP, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockBoneCP);
		this.testClass.run();
		verify(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection);
	}

	
	/** Tests that a connection that has been idle for more than the set time is closed off. 
	 * @throws SQLException */
	@Test
	public void testIdleConnectionIsKilled() throws SQLException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(2);
		fakeFreeConnections.add(mockConnection);
		fakeFreeConnections.add(mockConnection);
		
		expect(mockBoneCP.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(0).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		
		// connection should be closed
		mockConnection.internalClose();
		// partition should be set to allow creation of new ones
		mockConnectionPartition.setUnableToCreateMoreTransactions(false);

		replay(mockBoneCP, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockBoneCP);
		this.testClass.run();
		verify(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection);
	}

	/** Tests that a connection gets to receive a keep-alive. 
	 * @throws SQLException 
	 * @throws InterruptedException */
	@Test
	public void testIdleConnectionIsSentKeepAlive() throws SQLException, InterruptedException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockBoneCP.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockBoneCP.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		mockBoneCP.releaseInAnyFreePartition((ConnectionHandle)anyObject(), (ConnectionPartition)anyObject());
		
		// connection should be closed
		mockConnection.setConnectionLastReset(anyLong());

		replay(mockBoneCP, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockBoneCP);
		this.testClass.run();
		verify(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection);
	}

	/** Tests that an active connection that fails the connection is alive test will get closed. 
	 * @throws SQLException 
	 * @throws InterruptedException */
	@Test
	public void testIdleConnectionFailedKeepAlive() throws SQLException, InterruptedException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockBoneCP.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockBoneCP.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(false).anyTimes();
		
		// connection should be closed
		mockConnection.internalClose();
		// partition should be set to allow creation of new ones
		mockConnectionPartition.setUnableToCreateMoreTransactions(false);

		
		replay(mockBoneCP, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockBoneCP);
		this.testClass.run();
		verify(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection);
	}


	/** Tests fake exceptions, connection should be shutdown if the scheduler was marked as going down. Mostly for code coverage. 
	 * @throws SQLException 
	 * @throws InterruptedException */
	@Test
	public void testInterruptedException() throws SQLException, InterruptedException {
		ArrayBlockingQueue<ConnectionHandle> fakeFreeConnections = new ArrayBlockingQueue<ConnectionHandle>(1);
		fakeFreeConnections.add(mockConnection);
		
		config.setIdleConnectionTestPeriod(1);
		expect(mockBoneCP.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockBoneCP.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(true);
		mockBoneCP.releaseInAnyFreePartition((ConnectionHandle)anyObject(), (ConnectionPartition)anyObject());
		expectLastCall().andThrow(new InterruptedException());
		// connection should be closed
		mockConnection.internalClose();
		// partition should be set to allow creation of new ones
		mockConnectionPartition.setUnableToCreateMoreTransactions(false);
		
		
		replay(mockBoneCP, mockConnection, mockConnectionPartition, mockExecutor);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockBoneCP);
		this.testClass.run();
		verify(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection);
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
		expect(mockBoneCP.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockBoneCP.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(true).anyTimes();
		expect(mockExecutor.isShutdown()).andReturn(false);
		mockBoneCP.releaseInAnyFreePartition((ConnectionHandle)anyObject(), (ConnectionPartition)anyObject());
		expectLastCall().andThrow(new InterruptedException());
		mockLogger.error(anyObject());
		
		replay(mockBoneCP, mockConnection, mockConnectionPartition, mockExecutor, mockLogger);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockBoneCP);
		Field loggerField = this.testClass.getClass().getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(this.testClass, mockLogger);
		this.testClass.run();
		verify(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
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
		expect(mockBoneCP.getConfig()).andReturn(config).anyTimes();
		expect(mockConnectionPartition.getFreeConnections()).andReturn(fakeFreeConnections).anyTimes();
		expect(mockConnectionPartition.getMinConnections()).andReturn(10).once();
		expect(mockConnection.isPossiblyBroken()).andReturn(false);
		expect(mockConnection.getConnectionLastUsed()).andReturn(0L);
		expect(mockBoneCP.isConnectionHandleAlive((ConnectionHandle)anyObject())).andReturn(false).anyTimes();
		
		// connection should be closed
		mockConnection.internalClose();
		expectLastCall().andThrow(new SQLException());

		
		replay(mockBoneCP, mockConnection, mockConnectionPartition, mockExecutor, mockLogger);
		this.testClass = new ConnectionTesterThread(mockConnectionPartition, mockExecutor, mockBoneCP);
		Field loggerField = this.testClass.getClass().getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(this.testClass, mockLogger);
		this.testClass.run();
		verify(mockBoneCP, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
	}
}
