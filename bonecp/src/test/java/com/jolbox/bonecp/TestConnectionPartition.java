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

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import com.google.common.base.FinalizableReferenceQueue;
import com.jolbox.bonecp.proxy.ConnectionProxy;
import org.junit.Test;
import org.slf4j.Logger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
/**
 * @author wwadge
 *
 */
public class TestConnectionPartition {
	/** mock handle. */
	private BoneCP mockPool = createNiceMock(BoneCP.class);
	/** mock handle. */
	private static Logger mockLogger;
	/** mock handle. */
	private static BoneCPConfig mockConfig;
	/** mock handle. */
	private static ConnectionPartition testClass;


	/**
	 * Tests the constructor. Makes sure release helper threads are launched (+ setup other config items).
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testConstructor() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		mockConfig = createNiceMock(BoneCPConfig.class);
		expect(mockConfig.getAcquireIncrement()).andReturn(1).anyTimes();
		expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getMinConnectionsPerPartition()).andReturn(1).anyTimes();
		expect(mockConfig.getUsername()).andReturn("testuser").anyTimes();
		expect(mockConfig.getPassword()).andReturn("testpass").anyTimes();
		expect(mockConfig.getJdbcUrl()).andReturn("testurl").anyTimes();
		expect(mockConfig.getPoolName()).andReturn("Junit test").anyTimes();
		expect(mockConfig.isDisableConnectionTracking()).andReturn(false).anyTimes();
		Map<Connection, Reference<ConnectionHandle>> refs = new HashMap<Connection, Reference<ConnectionHandle>>();
		expect(this.mockPool.getFinalizableRefs()).andReturn(refs).anyTimes();
		expect(this.mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		replay(mockPool, mockConfig);
		testClass = new ConnectionPartition(this.mockPool);
		mockLogger = TestUtils.mockLogger(testClass.getClass());
		makeThreadSafe(mockLogger, true);
		mockLogger.error((String)anyObject());
		expectLastCall().anyTimes();
		replay(mockLogger);
		verify(this.mockPool, mockConfig);
		reset(this.mockPool, mockConfig);
	}

	/**
	 * Test method for created connections.
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testUpdateCreatedConnections() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		int count = testClass.getCreatedConnections();
		testClass.updateCreatedConnections(5);
		assertEquals(count+5, testClass.getCreatedConnections());
	}

	/**
	 * Test method for created connections.
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testUpdateCreatedConnectionsWithException() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	
		// Test #2: Same test but fake an exception 
		ReentrantReadWriteLock mockLock = createNiceMock(ReentrantReadWriteLock.class);
		WriteLock mockWriteLock = createNiceMock(WriteLock.class);

		Field field = testClass.getClass().getDeclaredField("statsLock");
		field.setAccessible(true);
		ReentrantReadWriteLock oldLock = (ReentrantReadWriteLock) field.get(testClass);
		field.set(testClass, mockLock);
		expect(mockLock.writeLock()).andThrow(new RuntimeException()).once().andReturn(mockWriteLock).once();
		mockWriteLock.lock();
		expectLastCall().once();
		replay(mockLock, mockWriteLock);

		try{
			testClass.updateCreatedConnections(5);
			fail("Should have thrown an exception");
		} catch (Throwable t){
			//do nothing
		}
		verify(mockLock);
		field.set(testClass, oldLock);

	}

	/**
	 * Test method for freeConnections
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFreeConnection() throws SQLException  {
		int count = testClass.getCreatedConnections();

		BoundedLinkedTransferQueue<ConnectionHandle> freeConnections = createNiceMock(BoundedLinkedTransferQueue.class);
		makeThreadSafe(freeConnections, true);
		testClass.setFreeConnections(freeConnections);
		assertEquals(freeConnections, testClass.getFreeConnections());
		reset(this.mockPool);
		Map<Connection, Reference<ConnectionHandle>> refs = new HashMap<Connection, Reference<ConnectionHandle>>();
		expect(this.mockPool.getFinalizableRefs()).andReturn(refs).anyTimes();
		FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
		expect(this.mockPool.getFinalizableRefQueue()).andReturn(finalizableRefQueue).anyTimes();

		ConnectionHandle mockConnectionHandle = createNiceMock(ConnectionHandle.class);
		expect(mockConnectionHandle.getPool()).andReturn(this.mockPool).anyTimes();
		expect(freeConnections.offer(mockConnectionHandle)).andReturn(true).anyTimes();
		replay(mockConnectionHandle, freeConnections, this.mockPool);
		testClass.addFreeConnection(mockConnectionHandle);
		verify(mockConnectionHandle, freeConnections);
		assertEquals(count+1, testClass.getCreatedConnections());
		assertEquals(0, testClass.getRemainingCapacity());

	}

	/** fail to offer a new connection.
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFreeConnectionFailing() throws SQLException  {
		int count = testClass.getCreatedConnections();

		BoundedLinkedTransferQueue<ConnectionHandle> freeConnections = createNiceMock(BoundedLinkedTransferQueue.class);
		makeThreadSafe(freeConnections, true);

		testClass.setFreeConnections(freeConnections);
		assertEquals(freeConnections, testClass.getFreeConnections());
		reset(this.mockPool);
		Map<Connection, Reference<ConnectionHandle>> refs = new HashMap<Connection, Reference<ConnectionHandle>>();
		expect(this.mockPool.getFinalizableRefs()).andReturn(refs).anyTimes();
		
		FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
		expect(this.mockPool.getFinalizableRefQueue()).andReturn(finalizableRefQueue).anyTimes();
		ConnectionHandle mockConnectionHandle = createNiceMock(ConnectionHandle.class);
		expect(mockConnectionHandle.getPool()).andReturn(this.mockPool).anyTimes();
		expect(freeConnections.offer(mockConnectionHandle)).andReturn(false);
		
		mockConnectionHandle.internalClose();
		expectLastCall().once();

		expect(freeConnections.remainingCapacity()).andReturn(1).anyTimes();
		Connection mockRealConnection = createNiceMock(Connection.class);
		expect(mockConnectionHandle.getInternalConnection()).andReturn(mockRealConnection).anyTimes();
		testClass.pool = this.mockPool;
		replay(mockConnectionHandle, mockRealConnection, freeConnections, this.mockPool);
		testClass.addFreeConnection(mockConnectionHandle);
		verify(mockConnectionHandle, freeConnections);
		assertEquals(count, testClass.getCreatedConnections());
		assertEquals(1, testClass.getRemainingCapacity());

	}

	/**
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testGetCreatedConnections() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		ReentrantReadWriteLock mockLock = createNiceMock(ReentrantReadWriteLock.class);
		ReadLock mockReadLock = createNiceMock(ReadLock.class);

		Field field = testClass.getClass().getDeclaredField("statsLock");
		field.setAccessible(true);
		ReentrantReadWriteLock oldLock = (ReentrantReadWriteLock) field.get(testClass);
		field.set(testClass, mockLock);
		expect(mockLock.readLock()).andThrow(new RuntimeException()).once().andReturn(mockReadLock).once();
		mockReadLock.lock();
		expectLastCall().once();
		replay(mockLock, mockReadLock);

		try{
			testClass.getCreatedConnections();
			fail("Should have thrown an exception");
		} catch (Throwable t){
			//do nothing
		}
		verify(mockLock);
		field.set(testClass, oldLock);
	}

	/**
	 * Test method for config related stuff.
	 */
	@Test
	public void testConfigStuff() {
		assertEquals("testurl", testClass.getUrl());
		assertEquals("testuser", testClass.getUsername());
		assertEquals("testpass", testClass.getPassword());
		assertEquals(1, testClass.getMaxConnections());
		assertEquals(1, testClass.getMinConnections());
		assertEquals(1, testClass.getAcquireIncrement());
	}


	/**
	 * Test method for unable to create more transactions.
	 */
	@Test
	public void testUnableToCreateMoreTransactionsFlag() {
		testClass.setUnableToCreateMoreTransactions(true);
		assertEquals(testClass.isUnableToCreateMoreTransactions(), true);
	}


	/** Test finalizer.
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	@Test
	public void testFinalizer() throws SQLException, InterruptedException{
		ConnectionHandle mockConnectionHandle = createNiceMock(ConnectionHandle.class); 
		expect(mockConnectionHandle.isInReplayMode()).andReturn(true).anyTimes();
		Connection mockConnection = createNiceMock(Connection.class);
		Connection connection = MemorizeTransactionProxy.memorize(mockConnection, mockConnectionHandle);
		expect(mockConnectionHandle.getInternalConnection()).andReturn(connection).anyTimes();
		mockConnection.close();
		expectLastCall().once();
		reset(this.mockPool);
		Map<Connection, Reference<ConnectionHandle>> refs = new HashMap<Connection, Reference<ConnectionHandle>>();
		expect(this.mockPool.getFinalizableRefs()).andReturn(refs).anyTimes();
		FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
		expect(this.mockPool.getFinalizableRefQueue()).andReturn(finalizableRefQueue).anyTimes();
		expect(mockConnectionHandle.getPool()).andReturn(this.mockPool).anyTimes();
		expect(this.mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		expect(mockConfig.getPoolName()).andReturn("foo").once();
		makeThreadSafe(this.mockPool, true);

		replay(mockConnection, mockConnectionHandle, this.mockPool, mockConfig);


			testClass.trackConnectionFinalizer(mockConnectionHandle);
			reset(mockConnectionHandle);

			mockConnectionHandle = null; // prompt GC to kick in
			for (int i=0; i < 500; i++){
				System.gc();System.gc();System.gc();
				Thread.sleep(20);
				try{
					verify(mockConnection);
					break; // we succeeded
				} catch (Throwable t){
					//				t.printStackTrace();
					// do nothing, try again
					Thread.sleep(20);
				}
			}
	}


	/** Test finalizer with error.
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	@Test
	public void testFinalizerCoverageException() throws SQLException, InterruptedException{
		ConnectionHandle mockConnectionHandle = createNiceMock(ConnectionHandle.class);
		Connection mockConnection = createNiceMock(Connection.class);
		expect(mockConnectionHandle.getInternalConnection()).andReturn(mockConnection).anyTimes();
		mockConnection.close();
		expectLastCall().andThrow(new SQLException("fake reason")).once();
		reset(this.mockPool);
		Map<Connection, Reference<ConnectionHandle>> refs = new HashMap<Connection, Reference<ConnectionHandle>>();
		expect(this.mockPool.getFinalizableRefs()).andReturn(refs).anyTimes();
		FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
		expect(this.mockPool.getFinalizableRefQueue()).andReturn(finalizableRefQueue).anyTimes();
		expect(mockConnectionHandle.getPool()).andReturn(this.mockPool).anyTimes();

		replay(mockConnectionHandle, mockConnection, this.mockPool);
		testClass.trackConnectionFinalizer(mockConnectionHandle);
		testClass.statsLock = null; // this makes it blow up.
		reset(mockLogger);
		mockLogger.error((String)anyObject());
		expectLastCall().anyTimes();
		replay(mockLogger);
		reset(mockConnectionHandle);
		mockConnectionHandle = null; // prompt GC to kick in
		for (int i=0; i < 100; i++){
			System.gc();System.gc();System.gc();
			Thread.sleep(20);
			try{
				verify(mockLogger);
				break; // we succeeded
			} catch (Throwable t){
				// do nothing, try again
				Thread.sleep(20);
			}
		}
	}

	/** Test finalizer.
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testFinalizerException2() throws SQLException, InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		ConnectionHandle mockConnectionHandle = createNiceMock(ConnectionHandle.class); 
		expect(mockConnectionHandle.isInReplayMode()).andReturn(true).anyTimes();
		Connection mockConnection = createNiceMock(Connection.class);
		Connection connection = new MemorizeTransactionProxyDummy(null,null).memorizeDummy(mockConnection, mockConnectionHandle);
		expect(mockConnectionHandle.getInternalConnection()).andReturn(connection).anyTimes();
		makeThreadSafe(mockConnectionHandle, true);
		makeThreadSafe(mockConnection, true);
		mockLogger = TestUtils.mockLogger(testClass.getClass());
		
		reset(mockLogger);
		makeThreadSafe(mockLogger, true);
		reset(this.mockPool);
		Map<Connection, Reference<ConnectionHandle>> refs = new HashMap<Connection, Reference<ConnectionHandle>>();
		expect(this.mockPool.getFinalizableRefs()).andReturn(refs).anyTimes();
		FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
		expect(this.mockPool.getFinalizableRefQueue()).andReturn(finalizableRefQueue).anyTimes();
		expect(mockConnectionHandle.getPool()).andReturn(this.mockPool).anyTimes();

		replay(mockConnection, mockConnectionHandle, this.mockPool);

		testClass.trackConnectionFinalizer(mockConnectionHandle);
		reset(mockConnectionHandle);
		mockConnectionHandle = null; // prompt GC to kick in
		for (int i=0; i < 100; i++){
			System.gc();System.gc();System.gc();
			Thread.sleep(20);
			try{
				verify(mockConnection);
				break; // we succeeded
			} catch (Throwable t){
				t.printStackTrace();
				// do nothing, try again
				Thread.sleep(20);
			}
		}
	}
	/** Fake proxy
	 * @author wallacew
	 *
	 */
	class MemorizeTransactionProxyDummy  extends MemorizeTransactionProxy {

		/** Fake constructor
		 * @param target
		 * @param connectionHandle
		 */
		public MemorizeTransactionProxyDummy(Connection target,
				ConnectionHandle connectionHandle) {
			// do nothing
		}

		/** Just throw errors for test
		 * @param target
		 * @param connectionHandle
		 * @return proxy
		 */
		protected Connection memorizeDummy(final Connection target, final ConnectionHandle connectionHandle) {

			return (Connection) Proxy.newProxyInstance(
					ConnectionProxy.class.getClassLoader(),
					new Class[] {ConnectionProxy.class},
					new MemorizeTransactionProxyDummy(target, connectionHandle));
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable {
			if (method.getName() != "hashCode"){
				throw new RuntimeException("fake error");
			} 

			return 1; 
		}

	}

}