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


import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.jolbox.bonecp.hooks.ConnectionHook;
import com.jolbox.bonecp.hooks.CoverageHook;

/**
 * Mock unit testing for Connection Handle class.
 * @author wwadge
 *
 */
public class TestConnectionHandle {
	/** Test class handle. */
	private static ConnectionHandle testClass;
	/** Mock handle. */
	private static ConnectionHandle mockConnection;
	/** Mock handle. */
	private static IStatementCache mockPreparedStatementCache;
	/** Mock handle. */
	private static IStatementCache mockCallableStatementCache;
	/** Mock handle. */
	private static BoneCP mockPool;
	/** Mock handle. */
	private static Logger mockLogger;
	/** Mock handle. */
	private static StatementCache testStatementCache;


	/** Mock setup.
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockPreparedStatementCache = createNiceMock(IStatementCache.class);
		mockCallableStatementCache = createNiceMock(IStatementCache.class);

		mockLogger = createNiceMock(Logger.class);
		makeThreadSafe(mockLogger, true);
		mockPool = createNiceMock(BoneCP.class);
		testClass = new ConnectionHandle(mockConnection, mockPreparedStatementCache, mockCallableStatementCache, mockPool);
		testStatementCache = new StatementCache(100, 100);
		Field field = testClass.getClass().getDeclaredField("logger");
		field.setAccessible(true);
		field.set(null, mockLogger);
	}

	/** Reset everything.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Before
	public void before() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		reset(mockConnection, mockPreparedStatementCache, mockPool);
		Field field = testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.set(testClass, false);

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
		skipTests.add("clearStatementHandles");
		skipTests.add("sendInitSQL");
		skipTests.add("$VRi"); // this only comes into play when code coverage is started. Eclemma bug?

		CommonTestUtils.testStatementBounceMethod(mockConnection, testClass, skipTests, mockConnection);
	}

	/** Test marking of possibly broken status.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testMarkPossiblyBroken() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Field field = testClass.getClass().getDeclaredField("possiblyBroken");
		field.setAccessible(true);
		field.set(testClass, false);
		testClass.markPossiblyBroken(null);
		Assert.assertTrue(field.getBoolean(testClass));

		// Test that a db fatal error will lead to the pool being instructed to terminate all connections (+ log)
		mockPool.terminateAllConnections();
		mockLogger.error((String)anyObject(), anyObject());
		replay(mockPool);
		testClass.markPossiblyBroken(new SQLException("test", "08001"));
		verify(mockPool);


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
	@Test 
	public void testClose() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException{

		Field field = testClass.getClass().getDeclaredField("doubleCloseCheck");
		field.setAccessible(true);
		field.set(testClass, true);

		testClass.renewConnection();
		mockPool.releaseConnection((Connection)anyObject());
		expectLastCall().once().andThrow(new SQLException()).once();
		replay(mockPool);

		testClass.close();


		// logically mark the connection as closed
		field = testClass.getClass().getDeclaredField("logicallyClosed");

		field.setAccessible(true);
		Assert.assertTrue(field.getBoolean(testClass));

		

		testClass.renewConnection();
		try{
			testClass.close(); // 2nd time should throw an exception
			fail("Should have thrown an exception");
		} catch (Throwable t){
			// do nothing.
		}
		
		verify(mockPool);
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
		expect(mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		expect(mockConfig.getInitSQL()).andReturn("test").anyTimes();
		Field field = testClass.getClass().getDeclaredField("connection");
		field.setAccessible(true);
		field.set(testClass, mockConnection);
		
		Statement mockStatement = createNiceMock(Statement.class);
		ResultSet mockResultSet = createNiceMock(ResultSet.class);
		expect(mockConnection.createStatement()).andReturn(mockStatement).once();
		expect(mockStatement.executeQuery("test")).andReturn(mockResultSet).once();
		mockResultSet.close();
		expectLastCall().once();
		
		replay(mockConfig, mockPool, mockConnection, mockStatement, mockResultSet);
		testClass.sendInitSQL();
		verify(mockConfig, mockPool, mockConnection,  mockStatement, mockResultSet);

		
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
		Field field = testClass.getClass().getDeclaredField("doubleCloseCheck");
		field.setAccessible(true);
		field.set(testClass, true);

		field = testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.set(testClass, true);

		field = testClass.getClass().getDeclaredField("doubleCloseException");
		field.setAccessible(true);
		field.set(testClass, "fakeexception");
		mockLogger.error((String)anyObject(), anyObject());
		expectLastCall().once();
		
		mockPool.releaseConnection((Connection)anyObject());
		expectLastCall().once().andThrow(new SQLException()).once();
		replay(mockLogger, mockPool);

		testClass.close();

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
		Field field = testClass.getClass().getDeclaredField("statementHandles");

		field.setAccessible(true);
		field.set(testClass, mockStatementHandles); 

		expect(mockStatementHandles.poll()).andReturn(mockStatement).once().andReturn(null).once();
		mockStatement.internalClose();
		expectLastCall().once();
		mockConnection.close();
		expectLastCall().once().andThrow(new SQLException()).once();
		replay(mockStatement, mockConnection, mockStatementHandles);
		testClass.internalClose();
		try{
			testClass.internalClose(); //2nd time should throw exception
			fail("Should have thrown an exception");
		} catch (Throwable t){
			// do nothing.
		}

		verify(mockStatement, mockConnection, mockStatementHandles);
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

		testClass.renewConnection();

		// call the method (should not throw an exception)
		Method method = testClass.getClass().getDeclaredMethod("checkClosed");
		method.setAccessible(true);
		method.invoke(testClass);

		// logically mark the connection as closed
		Field field = testClass.getClass().getDeclaredField("logicallyClosed");

		field.setAccessible(true);
		field.set(testClass, true);
		try{
			method.invoke(testClass);
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
		Field field = testClass.getClass().getDeclaredField("doubleCloseCheck");
		field.setAccessible(true);
		field.set(testClass, true);
		
		field = testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.set(testClass, true);
		
		testClass.renewConnection();
		assertFalse(field.getBoolean(testClass));


	}
	/** Tests various getter/setters.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testSettersGetters() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		ConnectionPartition mockPartition = createNiceMock(ConnectionPartition.class);
		testClass.setOriginatingPartition(mockPartition);
		assertEquals(mockPartition, testClass.getOriginatingPartition());

		testClass.setConnectionLastReset(123);
		assertEquals(testClass.getConnectionLastReset(), 123);

		testClass.setConnectionLastUsed(456);
		assertEquals(testClass.getConnectionLastUsed(), 456);

		Field field = testClass.getClass().getDeclaredField("possiblyBroken");
		field.setAccessible(true);
		field.setBoolean(testClass, true);
		assertTrue(testClass.isPossiblyBroken());
		
		Object debugHandle = new Object();
		testClass.setDebugHandle(debugHandle);
		assertEquals(debugHandle, testClass.getDebugHandle());
		
		field = testClass.getClass().getDeclaredField("connection");
		field.setAccessible(true);
		field.set(testClass, mockConnection);
		assertEquals(mockConnection, testClass.getRawConnection());
		
		field = testClass.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.setBoolean(testClass, true);
		assertTrue(testClass.isLogicallyClosed());
		
		testClass.setLogStatementsEnabled(true);
		assertTrue(testClass.isLogStatementsEnabled());
		
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
	@SuppressWarnings("unchecked")
	private void prepareStatementTest(Class... args) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		Object[] params = new Object[args.length];
		for (int i=0; i < args.length; i++){
			params[i] = CommonTestUtils.instanceMap.get(args[i]);
		}

		Method prepStatementMethod = testClass.getClass().getMethod("prepareStatement", args);

		PreparedStatementHandle mockStatement = createNiceMock(PreparedStatementHandle.class);
		ConcurrentLinkedQueue<Statement> mockStatementHandles = createNiceMock(ConcurrentLinkedQueue.class);
		Field field = testClass.getClass().getDeclaredField("statementHandles");
		field.setAccessible(true);
		field.set(testClass, mockStatementHandles);

		testClass.renewConnection(); // logically open the connection

		// fetching a statement that is found in cache. Statement should be returned and marked as being (logically) open
		doStatementMock(mockPreparedStatementCache, mockStatement, params, args);

		mockStatement.setLogicallyOpen();
		expectLastCall();
		replay(mockStatement, mockPreparedStatementCache);
		prepStatementMethod.invoke(testClass, params);
		verify(mockStatement, mockPreparedStatementCache);

		reset(mockStatement, mockPreparedStatementCache, mockStatementHandles);


		// test for a cache miss
		doStatementMock(mockPreparedStatementCache, null, params, args);
		// we should be creating the preparedStatement because it's not in the cache
		expect(prepStatementMethod.invoke(mockConnection, params)).andReturn(mockStatement);
		// we should be tracking this statement
		expect(mockStatementHandles.add(mockStatement)).andReturn(true);

		replay(mockStatement, mockPreparedStatementCache, mockConnection);
		prepStatementMethod.invoke(testClass, params);
		verify(mockStatement, mockPreparedStatementCache, mockConnection);


		// test for cache miss + sql exception
		reset(mockStatement, mockPreparedStatementCache, mockConnection);

		Method mockConnectionPrepareStatementMethod = mockConnection.getClass().getMethod("prepareStatement", args);

		//		expect(mockPreparedStatementCache.get((String)anyObject())).andReturn(null).once();
		doStatementMock(mockPreparedStatementCache, null, params, args);

		// we should be trying to create the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareStatementMethod.invoke(mockConnection, params)).andThrow(new SQLException("test", "Z"));

		replay(mockStatement, mockPreparedStatementCache, mockConnection);
		try{
			prepStatementMethod.invoke(testClass, params);
			fail("Should have thrown an exception");
		} catch (Throwable t) {
			// do nothing
		}

		verify(mockStatement, mockPreparedStatementCache, mockConnection);



		// test for no cache defined
		reset(mockStatement, mockPreparedStatementCache, mockConnection, mockStatementHandles);
		field = testClass.getClass().getDeclaredField("preparedStatementCache");
		field.setAccessible(true);
		IStatementCache oldCache = (IStatementCache) field.get(testClass);
		field.set(testClass, null);


		// we should be creating the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareStatementMethod.invoke(mockConnection, params)).andReturn(mockStatement);
		// we should be tracking this statement
		expect(mockStatementHandles.add(mockStatement)).andReturn(true);

		replay(mockStatement, mockPreparedStatementCache, mockConnection, mockStatementHandles);
		prepStatementMethod.invoke(testClass, params);
		verify(mockStatement, mockPreparedStatementCache, mockConnection);
		// restore sanity
		field.set(testClass, oldCache);

		reset(mockStatement, mockPreparedStatementCache, mockConnection, mockStatementHandles);

	}

	/** Mock setup.
	 * @param cache 
	 * @param returnVal 
	 * @param params
	 * @param args
	 */
	private void doStatementMock(IStatementCache cache, Statement returnVal, Object[] params, Class<?>... args) {
		expect(cache.get((String)anyObject())).andReturn(returnVal).anyTimes();
//		expect(cache.calculateCacheKey((String)anyObject())).andReturn(testStatementCache.calculateCacheKey((String)params[0])).anyTimes();

		if (args.length == 2) {
			if (params[1].getClass().equals(Integer.class)){

				expect(cache.calculateCacheKey((String)anyObject(), anyInt())).andReturn(testStatementCache.calculateCacheKey((String)params[0], (Integer)params[1])).anyTimes();
				expect(cache.get((String)anyObject(), anyInt())).andReturn(returnVal).anyTimes();
			}			
			expect(cache.calculateCacheKey((String)anyObject(), aryEq(new int[]{0,1}))).andReturn(testStatementCache.calculateCacheKey((String)params[0], new int[]{0,1})).anyTimes();
			expect(cache.get((String)anyObject(), aryEq(new int[]{0,1}))).andReturn(returnVal).anyTimes();

			expect(cache.calculateCacheKey((String)anyObject(), (String[])anyObject())).andReturn(testStatementCache.calculateCacheKey((String)params[0], new String[]{"test", "bar"})).anyTimes();
			expect(cache.get((String)anyObject(), (String[])anyObject())).andReturn(returnVal).anyTimes();
		}
		if (args.length == 3) {
			expect(cache.calculateCacheKey((String)anyObject(), anyInt(), anyInt())).andReturn(testStatementCache.calculateCacheKey((String)params[0], (Integer)params[1], (Integer)params[2])).anyTimes();
			expect(cache.get((String)anyObject(), anyInt(), anyInt())).andReturn(returnVal).anyTimes();
		}
		if (args.length == 4) {
			expect(cache.calculateCacheKey((String)anyObject(), anyInt(), anyInt(), anyInt())).andReturn(testStatementCache.calculateCacheKey((String)params[0], (Integer)params[1], (Integer)params[2], (Integer)params[3])).anyTimes();
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
	@SuppressWarnings("unchecked")
	private void callableStatementTest(Class... args) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		Object[] params = new Object[args.length];
		for (int i=0; i < args.length; i++){
			params[i] = CommonTestUtils.instanceMap.get(args[i]);
		}

		Method prepCallMethod = testClass.getClass().getMethod("prepareCall", args);

		CallableStatementHandle mockStatement = createNiceMock(CallableStatementHandle.class);
		ConcurrentLinkedQueue<Statement> mockStatementHandles = createNiceMock(ConcurrentLinkedQueue.class);
		Field field = testClass.getClass().getDeclaredField("statementHandles");
		field.setAccessible(true);
		field.set(testClass, mockStatementHandles);

		testClass.renewConnection(); // logically open the connection

		// fetching a statement that is found in cache. Statement should be returned and marked as being (logically) open
		doStatementMock(mockCallableStatementCache, mockStatement, params, args);

//		expect(mockCallableStatementCache.get((String)anyObject())).andReturn(mockStatement).anyTimes();

		//	((StatementHandle)mockStatement).setLogicallyOpen();
		expectLastCall();
		replay(mockStatement, mockCallableStatementCache);
		prepCallMethod.invoke(testClass, params);
		verify(mockStatement, mockCallableStatementCache);

		reset(mockStatement, mockCallableStatementCache, mockStatementHandles);


		// test for a cache miss
		doStatementMock(mockCallableStatementCache, null, params, args);
//		expect(mockCallableStatementCache.get((String)anyObject())).andReturn(null).once();

		// we should be creating the preparedStatement because it's not in the cache
		expect(prepCallMethod.invoke(mockConnection, params)).andReturn(mockStatement);
		// we should be tracking this statement
		expect(mockStatementHandles.add(mockStatement)).andReturn(true);

		replay(mockStatement, mockCallableStatementCache, mockConnection, mockStatementHandles);
		prepCallMethod.invoke(testClass, params);
		verify(mockStatement, mockCallableStatementCache, mockConnection);


		// test for cache miss + sql exception
		reset(mockStatement, mockCallableStatementCache, mockConnection);

		Method mockConnectionPrepareCallMethod = mockConnection.getClass().getMethod("prepareCall", args);

//		expect(mockCallableStatementCache.get((String)anyObject())).andReturn(null).once();
		doStatementMock(mockCallableStatementCache, null, params, args);
	
		// we should be creating the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareCallMethod.invoke(mockConnection, params)).andThrow(new SQLException("test", "Z"));

		replay(mockStatement, mockCallableStatementCache, mockConnection);
		try{
			prepCallMethod.invoke(testClass, params);
			fail("Should have thrown an exception");
		} catch (Throwable t) {
			// do nothing
		}

		verify(mockStatement, mockCallableStatementCache, mockConnection);



		// test for no cache defined
		reset(mockStatement, mockCallableStatementCache, mockConnection);
		field = testClass.getClass().getDeclaredField("callableStatementCache");
		field.setAccessible(true);
		IStatementCache oldCache = (IStatementCache) field.get(testClass);
		field.set(testClass, null);


		// we should be creating the preparedStatement because it's not in the cache
		expect(mockConnectionPrepareCallMethod.invoke(mockConnection, params)).andReturn(mockStatement);

		replay(mockStatement, mockCallableStatementCache, mockConnection);
		prepCallMethod.invoke(testClass, params);
		verify(mockStatement, mockCallableStatementCache, mockConnection);
		// restore sanity
		field.set(testClass, oldCache);

		reset(mockStatement, mockCallableStatementCache, mockConnection);

	}

	/** Set Client Info test.
	 * @throws SQLClientInfoException
	 */
	@Test
	public void testSetClientInfo() throws SQLClientInfoException {
		Properties prop = new Properties();
		mockConnection.setClientInfo(prop);
		replay(mockConnection);
		testClass.setClientInfo(prop);
		verify(mockConnection);

		reset(mockConnection);

		String name = "name";
		String value = "val";
		mockConnection.setClientInfo(name, value);
		replay(mockConnection);
		testClass.setClientInfo(name, value);
		verify(mockConnection);

	}
	
	/** Tests that a thrown exception will call the onAcquireFail hook.
	 * @throws SQLException
	 */
	@Test
	public void testConstructorFail() throws SQLException{
		BoneCPConfig mockConfig = createNiceMock(BoneCPConfig.class);
		ConnectionHook mockConnectionHook = createNiceMock(CoverageHook.class);
		expect(mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(mockConnectionHook).once();
		expect(mockConnectionHook.onAcquireFail((Throwable)anyObject())).andReturn(false).once();
		replay(mockPool, mockConfig, mockConnectionHook);
		try{
			new ConnectionHandle("", "", "", mockPool);
			fail("Should throw an exception");
		} catch (Throwable t){
			// do nothing.
		}
		verify(mockPool, mockConfig, mockPool);
	}
	

}
