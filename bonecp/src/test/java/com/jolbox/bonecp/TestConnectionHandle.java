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


import java.lang.Thread.State;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.common.base.FinalizableReferenceQueue;
import com.jolbox.bonecp.hooks.AcquireFailConfig;
import com.jolbox.bonecp.hooks.ConnectionHook;
import com.jolbox.bonecp.hooks.CoverageHook;
import com.jolbox.bonecp.hooks.CustomHook;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Mock unit testing for Connection Handle class.
 * @author wwadge
 *
 */
public class TestConnectionHandle {
	/** Test class handle. */
	private ConnectionHandle testClass;
	/** Mock handle. */
	private ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
	/** Mock handle. */
	private IStatementCache mockPreparedStatementCache = createNiceMock(IStatementCache.class);
	/** Mock handle. */
	private IStatementCache mockCallableStatementCache = createNiceMock(IStatementCache.class);
	/** Mock handle. */
	private BoneCP mockPool = createNiceMock(BoneCP.class);
	/** Mock handle. */
	private Logger mockLogger;
	/** Mock handle. */
	private StatementCache testStatementCache = new StatementCache(100, false, new Statistics(this.mockPool));
	/** Config clone. */
	private BoneCPConfig config;

	/** Reset everything.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Before
	public void before() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		reset(this.mockConnection, this.mockPreparedStatementCache, this.mockPool, this.mockCallableStatementCache);
		
		this.config = CommonTestUtils.getConfigClone();
		this.mockPool.connectionStrategy = new DefaultConnectionStrategy(this.mockPool);
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();
		
		expect(this.mockPool.isStatementReleaseHelperThreadsConfigured()).andReturn(false).anyTimes();
		expect(this.mockConnection.getPool()).andReturn(this.mockPool).anyTimes();
		expect(this.mockConnection.isLogStatementsEnabled()).andReturn(true).anyTimes();
		replay(this.mockConnection, this.mockPool);

		this.config.setReleaseHelperThreads(0);
		this.config.setTransactionRecoveryEnabled(false);
		this.config.setStatementsCacheSize(1);
		this.config.setStatisticsEnabled(true);
		
		this.testClass = ConnectionHandle.createTestConnectionHandle(this.mockConnection, this.mockPreparedStatementCache, this.mockCallableStatementCache, this.mockPool);
		
		this.mockPool.closeConnectionWatch=true;
		this.mockLogger = TestUtils.mockLogger(testClass.getClass());

		Field field = this.testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.set(this.testClass, false);
		
		field = this.testClass.getClass().getDeclaredField("statisticsEnabled");
		field.setAccessible(true);
		field.set(this.testClass, true);
		
		field = this.testClass.getClass().getDeclaredField("statistics");
		field.setAccessible(true);
		field.set(this.testClass, new Statistics(this.mockPool));
		
		reset(this.mockConnection, this.mockPreparedStatementCache, this.mockPool, this.mockCallableStatementCache);
	}


	/** For test. */
	static int count=1;

	/** Tests obtaining internal connection.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void testObtainInternalConnection() throws SQLException, ClassNotFoundException{
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();

		this.testClass.url = "jdbc:mock:driver";
		this.config.setAcquireRetryDelayInMs(1);
		CustomHook testHook = new CustomHook();
		this.config.setConnectionHook(testHook);
		// make it fail the first time and succeed the second time
		expect(this.mockPool.getDbIsDown()).andReturn(new AtomicBoolean()).anyTimes();
		expect(this.mockPool.obtainRawInternalConnection()).andThrow(new SQLException()).once().andReturn(this.mockConnection).once();
		replay(this.mockPool);
		this.testClass.obtainInternalConnection(this.mockPool, "jdbc:mock");
		// get counts on our hooks
		
		assertEquals(1, testHook.fail);
		assertEquals(1, testHook.acquire);
		
		// Test 2: Same thing but without the hooks
		reset(this.mockPool);
		expect(this.mockPool.getDbIsDown()).andReturn(new AtomicBoolean()).anyTimes();
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();
		expect(this.mockPool.obtainRawInternalConnection()).andThrow(new SQLException()).once().andReturn(this.mockConnection).once();
		count=1;
		this.config.setConnectionHook(null);
		replay(this.mockPool);
		assertEquals(this.mockConnection, this.testClass.obtainInternalConnection(this.mockPool, "jdbc:mock"));
		
		// Test 3: Keep failing
		reset(this.mockPool);
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();
		expect(this.mockPool.obtainRawInternalConnection()).andThrow(new SQLException()).anyTimes();
		replay(this.mockPool);
		count=99;
		this.config.setAcquireRetryAttempts(2);
		try{
			this.testClass.obtainInternalConnection(this.mockPool, "jdbc:mock");
			fail("Should have thrown an exception");
		} catch (SQLException e){
			// expected behaviour
		}
		 
		//	Test 4: Get signalled to interrupt fail delay
		count=99;
		this.config.setAcquireRetryAttempts(2);
		this.config.setAcquireRetryDelayInMs(7000);
		final Thread currentThread = Thread.currentThread();

		try{
			new Thread(new Runnable() {
				
//				@Override
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
			this.testClass.obtainInternalConnection(this.mockPool, "jdbc:mock");
			fail("Should have thrown an exception");
		} catch (SQLException e){
			// expected behaviour
		}
		this.config.setAcquireRetryDelayInMs(10);
		
		
	}

	/** Test bounce of inner connection.
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@Test
	public void testStandardMethods() throws IllegalArgumentException, SecurityException,  IllegalAccessException, InvocationTargetException{
		Set<String> skipTests = new HashSet<String>();
		skipTests.add("close");
		skipTests.add("getConnection");
		skipTests.add("markPossiblyBroken");
		skipTests.add("trackStatement");
		skipTests.add("checkClosed");
		skipTests.add("isClosed");
		skipTests.add("internalClose");
		skipTests.add("prepareCall");
		skipTests.add("prepareStatement");
		skipTests.add("setClientInfo");
		skipTests.add("getConnectionLastUsed");
		skipTests.add("setConnectionLastUsed");
		skipTests.add("getConnectionLastReset");	
		skipTests.add("setConnectionLastReset");
		skipTests.add("isPossiblyBroken");	
		skipTests.add("getOriginatingPartition");
		skipTests.add("setOriginatingPartition");
		skipTests.add("renewConnection");
		skipTests.add("clearStatementCaches");
		skipTests.add("obtainInternalConnection");
		skipTests.add("refreshConnection");
		skipTests.add("recreateConnectionHandle");
		skipTests.add("fillConnectionFields");
		skipTests.add("createConnectionHandle");
		
		skipTests.add("sendInitSQL");
		skipTests.add("$VRi"); // this only comes into play when code coverage is started. Eclemma bug?
		expect(this.mockPool.isStatementReleaseHelperThreadsConfigured()).andReturn(false).anyTimes();
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();
		replay(this.mockPool);
		CommonTestUtils.testStatementBounceMethod(this.mockConnection, this.testClass, skipTests, this.mockConnection);
	}

	/** Test marking of possibly broken status.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testMarkPossiblyBroken() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Field field = this.testClass.getClass().getDeclaredField("possiblyBroken");
		field.setAccessible(true);
		field.set(this.testClass, false);
		this.testClass.markPossiblyBroken(new SQLException());
		Assert.assertTrue(field.getBoolean(this.testClass));

		// Test that a db fatal error will lead to the pool being instructed to terminate all connections (+ log)
		expect(this.mockPool.getDbIsDown()).andReturn(new AtomicBoolean()).anyTimes();
		this.mockPool.connectionStrategy.terminateAllConnections();
		this.mockLogger.error((String)anyObject(), anyObject());
		replay(this.mockPool);
		this.testClass.markPossiblyBroken(new SQLException("test", "08001"));
		verify(this.mockPool);


	}

	/** Test. */
	boolean interrupted = false;
	/** Test. */
	boolean started = false;
	/** Closing a connection handle should release that connection back in the pool and mark it as closed.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SQLException
	 * @throws InterruptedException 
	 */
	@Test 
	public void testClose() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException, InterruptedException{

		Field field = this.testClass.getClass().getDeclaredField("doubleCloseCheck");
		field.setAccessible(true);
		field.set(this.testClass, true);

		this.testClass.renewConnection();
		this.mockPool.releaseConnection((Connection)anyObject());
		expectLastCall().once().andThrow(new SQLException()).once();
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();
		replay(this.mockPool);
		
		// create a thread so that we can check that thread.interrupt was called during connection close.
//		final Thread thisThread = Thread.currentThread();
		Thread testThread = new Thread(new Runnable() {
			
//			@Override
			public void run() {
				try {
					TestConnectionHandle.this.started = true;
					while(true){
						Thread.sleep(20);
					}
//				} catch (InterruptedException e) {
//					TestConnectionHandle.this.interrupted = true;
				} catch (Exception e) {
					TestConnectionHandle.this.interrupted = true;

				}
			}
		});
		testThread.start();
		while (!this.started){
			Thread.sleep(20);
		}
		this.testClass.setThreadWatch(testThread);
		this.testClass.close();
		testThread.join();
		assertTrue(this.interrupted); // thread should have been interrupted 

		// logically mark the connection as closed
		field = this.testClass.getClass().getDeclaredField("logicallyClosed");

		field.setAccessible(true);
		Assert.assertTrue(field.getBoolean(this.testClass));
		assertTrue(this.testClass.isClosed());


		this.testClass.renewConnection();
		try{
			this.testClass.close(); // 2nd time should throw an exception
			fail("Should have thrown an exception");
		} catch (Throwable t){
			// do nothing.
		}

	}

	/** Tests sendInitialSQL method.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	@Test
	public void testSendInitialSQL() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException{
		
		BoneCPConfig mockConfig = createNiceMock(BoneCPConfig.class);
		expect(this.mockPool.getConfig()).andReturn(mockConfig).anyTimes();

		expect(mockConfig.getInitSQL()).andReturn("test").anyTimes();
		this.testClass.setInternalConnection(this.mockConnection);

		Statement mockStatement = createNiceMock(Statement.class);
		expect(this.mockConnection.createStatement()).andReturn(mockStatement).once();
		expect(mockStatement.execute("test")).andReturn(true).once();

		replay(mockConfig, this.mockPool, this.mockConnection, mockStatement);
		this.testClass.sendInitSQL();
		verify(mockConfig, this.mockPool, this.mockConnection,  mockStatement);


	}

	/**
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	@Test
	public void testDoubleClose() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException{
		Field field = this.testClass.getClass().getDeclaredField("doubleCloseCheck");
		field.setAccessible(true);
		field.set(this.testClass, true);

		field = this.testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.set(this.testClass, true);

		field = this.testClass.getClass().getDeclaredField("doubleCloseException");
		field.setAccessible(true);
		field.set(this.testClass, "fakeexception");
		this.mockLogger.error((String)anyObject(), anyObject());
		expectLastCall().once();

		this.mockPool.releaseConnection((Connection)anyObject());
		expectLastCall().once().andThrow(new SQLException()).once();
		replay(this.mockLogger, this.mockPool);

		this.testClass.close();

	}

	/** Closing a connection handle should release that connection back in the pool and mark it as closed.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	@Test 
	public void testInternalClose() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException{
		ConcurrentLinkedQueue<Statement> mockStatementHandles = createNiceMock(ConcurrentLinkedQueue.class);
		StatementHandle mockStatement = createNiceMock(StatementHandle.class);
		
		this.mockConnection.close();
		expectLastCall().once().andThrow(new SQLException()).once();
		
		Map<Connection, Reference<ConnectionHandle>> refs = new HashMap<Connection, Reference<ConnectionHandle>>();
    	expect(this.mockPool.getFinalizableRefs()).andReturn(refs).anyTimes();
    	FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();

    	expect(this.mockPool.getFinalizableRefQueue()).andReturn(finalizableRefQueue).anyTimes();
    	expect(this.mockConnection.getPool()).andReturn(this.mockPool).anyTimes();

		Field f = this.testClass.getClass().getDeclaredField("finalizableRefs");
		f.setAccessible(true);
		f.set(this.testClass, refs);
    	
		replay(mockStatement, this.mockConnection, mockStatementHandles, this.mockPool);
		this.testClass.internalClose();
		try{
			this.testClass.internalClose(); //2nd time should throw exception
			fail("Should have thrown an exception");
		} catch (Throwable t){
			// do nothing.
		}

		verify(mockStatement, this.mockConnection, mockStatementHandles);
	}


	/** Test for check closed routine.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@Test 
	public void testCheckClosed() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{

		this.testClass.renewConnection();

		// call the method (should not throw an exception)
		Method method = this.testClass.getClass().getDeclaredMethod("checkClosed");
		method.setAccessible(true);
		method.invoke(this.testClass);

		// logically mark the connection as closed
		Field field = this.testClass.getClass().getDeclaredField("logicallyClosed");

		field.setAccessible(true);
		field.set(this.testClass, true);
		try{
			method.invoke(this.testClass);
			fail("Should have thrown an exception");
		} catch (Throwable t){
			// do nothing.
		}
	}

	/** Test renewal of connection.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testRenewConnection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Field field = this.testClass.getClass().getDeclaredField("doubleCloseCheck");
		field.setAccessible(true);
		field.set(this.testClass, true);

		field = this.testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.set(this.testClass, true);

		this.testClass.renewConnection();
		assertFalse(field.getBoolean(this.testClass));


	}
	/** Tests various getter/setters.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testSettersGetters() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		ConnectionPartition mockPartition = createNiceMock(ConnectionPartition.class);
		this.testClass.setOriginatingPartition(mockPartition);
		assertEquals(mockPartition, this.testClass.getOriginatingPartition());

		this.testClass.setConnectionLastResetInMs(123);
		assertEquals(this.testClass.getConnectionLastResetInMs(), 123);
		assertEquals(this.testClass.getConnectionLastReset(), 123);

		this.testClass.setConnectionLastUsedInMs(456);
		assertEquals(this.testClass.getConnectionLastUsedInMs(), 456);
		assertEquals(this.testClass.getConnectionLastUsed(), 456);

		Field field = this.testClass.getClass().getDeclaredField("possiblyBroken");
		field.setAccessible(true);
		field.setBoolean(this.testClass, true);
		assertTrue(this.testClass.isPossiblyBroken());

		field = this.testClass.getClass().getDeclaredField("connectionCreationTimeInMs");
		field.setAccessible(true);
		field.setLong(this.testClass, 1234L);
		assertEquals(1234L, this.testClass.getConnectionCreationTime());
		
		Object debugHandle = new Object();
		this.testClass.setDebugHandle(debugHandle);
		assertEquals(debugHandle, this.testClass.getDebugHandle());

		this.testClass.setInternalConnection(this.mockConnection);
		assertEquals(this.mockConnection, this.testClass.getInternalConnection());
		assertEquals(this.mockConnection, this.testClass.getRawConnection());

		field = this.testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.setBoolean(this.testClass, true);
		assertTrue(this.testClass.isClosed());

		this.testClass.setLogStatementsEnabled(true);
		assertTrue(this.testClass.isLogStatementsEnabled());

		assertEquals(this.testClass.getPool(), this.mockPool);
		ArrayList<ReplayLog> testLog = new ArrayList<ReplayLog>();
		this.testClass.setReplayLog(testLog);
		assertEquals(this.testClass.getReplayLog(), testLog);
		this.testClass.setInReplayMode(true);
		assertTrue(this.testClass.isInReplayMode());
		this.testClass.setInReplayMode(false);
		
		this.testClass.threadUsingConnection = Thread.currentThread();
		assertEquals(Thread.currentThread(), this.testClass.getThreadUsingConnection());
		
		this.testClass.setThreadWatch(Thread.currentThread());
		assertEquals(Thread.currentThread(), this.testClass.getThreadWatch());
		
	}

	/**
	 *  Simple test.
	 */
	@Test
	public void testIsConnectionHandleAlive(){
		// just make sure this is bounced off to the right place
		reset(this.mockPool);
		expect(this.mockPool.isConnectionHandleAlive(this.testClass)).andReturn(true).once();
		replay(this.mockPool);
		this.testClass.isConnectionAlive();
		verify(this.mockPool);
	}
	
	/** Tests isExpired method.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testIsExpired() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Field field = this.testClass.getClass().getDeclaredField("maxConnectionAgeInMs");
		field.setAccessible(true);
		field.setLong(this.testClass, 1234L);
		assertTrue(this.testClass.isExpired(System.currentTimeMillis() + 9999L));
	}

	/** Prepare statement tests.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@Test
	public void testPrepareStatement() throws SecurityException, IllegalArgumentException,  NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		expect(this.mockPool.captureStackTrace((String)anyObject())).andReturn("").anyTimes();
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();
		replay(this.mockPool);
		this.config.setStatementsCacheSize(1);
		prepareStatementTest(String.class);
		prepareStatementTest(String.class, int.class);
		prepareStatementTest(String.class, int[].class);
		prepareStatementTest(String.class, String[].class);
		prepareStatementTest(String.class, int.class, int.class);
		prepareStatementTest(String.class, int.class, int.class, int.class);
	}

	/** Callable statement tests.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@Test
	public void testCallableStatement() throws SecurityException, IllegalArgumentException,  NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		expect(this.mockPool.captureStackTrace((String)anyObject())).andReturn("").anyTimes();
		expect(this.mockPool.getConfig()).andReturn(this.config).anyTimes();
		this.config.setStatisticsEnabled(true);
		
		replay(this.mockPool);

		callableStatementTest(String.class);
		callableStatementTest(String.class, int.class, int.class);
		callableStatementTest(String.class, int.class, int.class, int.class);
	}


	/** Test routine.
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("rawtypes")
	private void prepareStatementTest(  Class... args) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		Object[] params = new Object[args.length];
		for (int i=0; i < args.length; i++){
			params[i] = CommonTestUtils.instanceMap.get(args[i]);
		}

		Method prepStatementMethod = this.testClass.getClass().getMethod("prepareStatement", args);

		PreparedStatementHandle mockStatement = createNiceMock(PreparedStatementHandle.class);
		mockStatement.setOpenStackTrace((String)anyObject());
		expectLastCall().anyTimes();

		this.testClass.renewConnection(); // logically open the connection

		// fetching a statement that is found in cache. Statement should be returned and marked as being (logically) open
		doStatementMock(this.mockPreparedStatementCache, mockStatement, params, args);

//		mockStatement.setLogicallyOpen();
//		expectLastCall();
		replay(mockStatement, this.mockPreparedStatementCache);
		prepStatementMethod.invoke(this.testClass, params);
		verify(mockStatement, this.mockPreparedStatementCache);

		reset(mockStatement, this.mockPreparedStatementCache);


		// test for a cache miss
		doStatementMock(this.mockPreparedStatementCache, null, params, args);
		// we should be creating the preparedStatement because it's not in the cache
		expect(prepStatementMethod.invoke(this.mockConnection, params)).andReturn(mockStatement);

		replay(mockStatement, this.mockPreparedStatementCache, this.mockConnection);
		prepStatementMethod.invoke(this.testClass, params);
		verify(mockStatement, this.mockPreparedStatementCache, this.mockConnection);


		// test for cache miss + sql exception
		reset(mockStatement, this.mockPreparedStatementCache, this.mockConnection);

		Method mockConnectionPrepareStatementMethod = this.mockConnection.getClass().getMethod("prepareStatement", args);

		//		expect(this.mockPreparedStatementCache.get((String)anyObject())).andReturn(null).once();
		doStatementMock(this.mockPreparedStatementCache, null, params, args);

		// we should be trying to create the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareStatementMethod.invoke(this.mockConnection, params)).andThrow(new SQLException("test", "Z"));

		replay(mockStatement, this.mockPreparedStatementCache, this.mockConnection);
		try{
			prepStatementMethod.invoke(this.testClass, params);
			fail("Should have thrown an exception");
		} catch (Throwable t) {
			// do nothing
		}

		verify(mockStatement, this.mockPreparedStatementCache, this.mockConnection);



		// test for no cache defined
		reset(mockStatement, this.mockPreparedStatementCache, this.mockConnection);
		boolean oldState = this.testClass.statementCachingEnabled;

		this.testClass.statementCachingEnabled = false;

		// we should be creating the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareStatementMethod.invoke(this.mockConnection, params)).andReturn(mockStatement);

		replay(mockStatement, this.mockPreparedStatementCache, this.mockConnection);
		prepStatementMethod.invoke(this.testClass, params);
		verify(mockStatement, this.mockPreparedStatementCache, this.mockConnection);
		// restore sanity
		this.testClass.statementCachingEnabled = oldState;

		reset(mockStatement, this.mockPreparedStatementCache, this.mockConnection);

	}

	/** Mock setup.
	 * @param cache 
	 * @param returnVal 
	 * @param params
	 * @param args
	 */
	private void doStatementMock(IStatementCache cache, StatementHandle returnVal, Object[] params, Class<?>... args) {
		expect(cache.get((String)anyObject())).andReturn(returnVal).anyTimes();
		//		expect(cache.calculateCacheKey((String)anyObject())).andReturn(testStatementCache.calculateCacheKey((String)params[0])).anyTimes();

		if (args.length == 2) {
			if (params[1].getClass().equals(Integer.class)){

				expect(cache.calculateCacheKey((String)anyObject(), anyInt())).andReturn(this.testStatementCache.calculateCacheKey((String)params[0], (Integer)params[1])).anyTimes();
				expect(cache.get((String)anyObject(), anyInt())).andReturn(returnVal).anyTimes();
			}			
			expect(cache.calculateCacheKey((String)anyObject(), aryEq(new int[]{0,1}))).andReturn(this.testStatementCache.calculateCacheKey((String)params[0], new int[]{0,1})).anyTimes();
			expect(cache.get((String)anyObject(), aryEq(new int[]{0,1}))).andReturn(returnVal).anyTimes();

			expect(cache.calculateCacheKey((String)anyObject(), (String[])anyObject())).andReturn(this.testStatementCache.calculateCacheKey((String)params[0], new String[]{"test", "bar"})).anyTimes();
			expect(cache.get((String)anyObject(), (String[])anyObject())).andReturn(returnVal).anyTimes();
		}
		if (args.length == 3) {
			expect(cache.calculateCacheKey((String)anyObject(), anyInt(), anyInt())).andReturn(this.testStatementCache.calculateCacheKey((String)params[0], (Integer)params[1], (Integer)params[2])).anyTimes();
			expect(cache.get((String)anyObject(), anyInt(), anyInt())).andReturn(returnVal).anyTimes();
		}
		if (args.length == 4) {
			expect(cache.calculateCacheKey((String)anyObject(), anyInt(), anyInt(), anyInt())).andReturn(this.testStatementCache.calculateCacheKey((String)params[0], (Integer)params[1], (Integer)params[2], (Integer)params[3])).anyTimes();
			expect(cache.get((String)anyObject(), anyInt(), anyInt(), anyInt())).andReturn(returnVal).anyTimes();
		}

	}

	/** Test routine for callable statements.
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("all")
	private void callableStatementTest(Class... args) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		Object[] params = new Object[args.length];
		for (int i=0; i < args.length; i++){
			params[i] = CommonTestUtils.instanceMap.get(args[i]);
		}

		Method prepCallMethod = this.testClass.getClass().getMethod("prepareCall", args);

		CallableStatementHandle mockStatement = createNiceMock(CallableStatementHandle.class);
		ConcurrentLinkedQueue<Statement> mockStatementHandles = createNiceMock(ConcurrentLinkedQueue.class);

		
		this.testClass.renewConnection(); // logically open the connection

		// fetching a statement that is found in cache. Statement should be returned and marked as being (logically) open
		doStatementMock(this.mockCallableStatementCache, mockStatement, params, args);

		//		expect(this.mockCallableStatementCache.get((String)anyObject())).andReturn(mockStatement).anyTimes();

//		((StatementHandle)mockStatement).setLogicallyOpen();
//		expectLastCall();
		replay(mockStatement, this.mockCallableStatementCache);
		prepCallMethod.invoke(this.testClass, params);
		verify(mockStatement, this.mockCallableStatementCache);

		reset(mockStatement, this.mockCallableStatementCache, mockStatementHandles);


		// test for a cache miss
		doStatementMock(this.mockCallableStatementCache, null, params, args);

		// we should be creating the preparedStatement because it's not in the cache
		expect(prepCallMethod.invoke(this.mockConnection, params)).andReturn(mockStatement);
		// we should be tracking this statement
		expect(mockStatementHandles.add(mockStatement)).andReturn(true);

		replay(mockStatement, this.mockCallableStatementCache, this.mockConnection, mockStatementHandles);
		prepCallMethod.invoke(this.testClass, params);
		verify(mockStatement, this.mockCallableStatementCache, this.mockConnection);


		// test for cache miss + sql exception
		reset(mockStatement, this.mockCallableStatementCache, this.mockConnection);

		Method mockConnectionPrepareCallMethod = this.mockConnection.getClass().getMethod("prepareCall", args);

		//		expect(this.mockCallableStatementCache.get((String)anyObject())).andReturn(null).once();
		doStatementMock(this.mockCallableStatementCache, null, params, args);

		// we should be creating the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareCallMethod.invoke(this.mockConnection, params)).andThrow(new SQLException("test", "Z"));

		replay(mockStatement, this.mockCallableStatementCache, this.mockConnection);
		try{
			prepCallMethod.invoke(this.testClass, params);
			fail("Should have thrown an exception");
		} catch (Throwable t) {
			// do nothing
		}

		verify(mockStatement, this.mockCallableStatementCache, this.mockConnection);



		// test for no cache defined
		reset(mockStatement, this.mockCallableStatementCache, this.mockConnection);
		boolean oldState = this.testClass.statementCachingEnabled;

		this.testClass.statementCachingEnabled = false;


		// we should be creating the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareCallMethod.invoke(this.mockConnection, params)).andReturn(mockStatement);

		replay(mockStatement, this.mockCallableStatementCache, this.mockConnection);
		prepCallMethod.invoke(this.testClass, params);
		verify(mockStatement, this.mockCallableStatementCache, this.mockConnection);
		// restore sanity
		this.testClass.statementCachingEnabled = oldState;

		reset(mockStatement, this.mockCallableStatementCache, this.mockConnection);

	}

	/**
	 */
	// #ifdef JDK6
	@Test
	public void testSetClientInfo() {
		Properties prop = new Properties();
		try {
			this.mockConnection.setClientInfo(prop);
		replay(this.mockConnection);
		this.testClass.setClientInfo(prop);
		verify(this.mockConnection);

		reset(this.mockConnection);

		String name = "name";
		String value = "val";
		this.mockConnection.setClientInfo(name, value);
		replay(this.mockConnection);
		this.testClass.setClientInfo(name, value);
		verify(this.mockConnection);
		} catch (SQLClientInfoException e) {
			throw new RuntimeException(e);
		}

	}
	 // #endif JDK6
	
	/** Tests that a thrown exception will call the onAcquireFail hook.
	 * @throws SQLException
	 */
	@Test
	public void testConstructorFail() throws SQLException{
		BoneCPConfig mockConfig = createNiceMock(BoneCPConfig.class);
		ConnectionHook mockConnectionHook = createNiceMock(CoverageHook.class);
		expect(this.mockPool.getConfig()).andReturn(mockConfig).anyTimes();
//		expect(mockConfig.getReleaseHelperThreads()).andReturn(1).once();
		expect(mockConfig.getConnectionHook()).andReturn(mockConnectionHook).once();
		expect(mockConnectionHook.onAcquireFail((Throwable)anyObject(), (AcquireFailConfig)anyObject())).andReturn(false).once();
		replay(this.mockPool, mockConfig, mockConnectionHook);
		try{
			ConnectionHandle.createConnectionHandle("", "", "", this.mockPool);
			fail("Should throw an exception");
		} catch (Throwable t){
			// do nothing.
		}
		verify(this.mockPool, mockConfig, this.mockPool);
	}


	/**
	 * Test for clear statement caches.
	 */
	@Test
	public void testClearStatementCaches(){

		this.testClass.statementCachingEnabled = true;
		this.mockPreparedStatementCache.clear();
		expectLastCall().once();
		this.mockCallableStatementCache.clear();
		expectLastCall().once();

		replay(this.mockPreparedStatementCache, this.mockCallableStatementCache);
		this.testClass.clearStatementCaches(true);
		verify(this.mockPreparedStatementCache, this.mockCallableStatementCache);
		reset(this.mockPreparedStatementCache, this.mockCallableStatementCache);


		this.mockPool.closeConnectionWatch = true;
		this.mockPreparedStatementCache.checkForProperClosure();
		expectLastCall().once();
		this.mockCallableStatementCache.checkForProperClosure();
		expectLastCall().once();

		replay(this.mockPreparedStatementCache, this.mockCallableStatementCache);
		this.testClass.clearStatementCaches(false);
		verify(this.mockPreparedStatementCache, this.mockCallableStatementCache);

	}

	/**
	 * 
	 */
	@Test
	public void testStackTraceAndTxResolve(){
		this.testClass.setAutoCommitStackTrace("foo");
		this.testClass.getAutoCommitStackTrace();
		assertEquals("foo", this.testClass.getAutoCommitStackTrace());
		this.testClass.txResolved = true;
		assertTrue(this.testClass.isTxResolved());
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	@Test
	public void testAutoCommitSetToFalse() throws SQLException{
		this.testClass.detectUnresolvedTransactions = true;
		expect(this.mockPool.captureStackTrace((String)anyObject())).andReturn("foo").once();
		replay(this.mockPool);
		this.testClass.setAutoCommit(false);
		assertNotNull(this.testClass.getAutoCommitStackTrace());
	}

}