/**
 * 
 */
package com.jolbox.bonecp;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.IStatementCache;
import com.jolbox.bonecp.PreparedStatementHandle;
import com.jolbox.bonecp.StatementHandle;
/**
 * @author wwadge
 *
 */
public class TestStatementHandle {
	/** Class under test. */
	private static StatementHandle testClass;
	/** Mock class under test. */
	private static StatementHandle mockClass;
	/** Mock handles. */
	private static IStatementCache mockCallableStatementCache;
	/** Mock handles. */
	private static ConnectionHandle mockConnection;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mockClass = createNiceMock(PreparedStatementHandle.class);
		mockCallableStatementCache = createNiceMock(IStatementCache.class);
		mockConnection = createNiceMock(ConnectionHandle.class);

		testClass = new StatementHandle(mockClass, "", mockCallableStatementCache, mockConnection, "testSQL");

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		reset(mockClass, mockCallableStatementCache, mockConnection);
	}

	/** Test that each method will result in an equivalent bounce on the inner statement (+ test exceptions)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@Test
	public void testStandardBounceMethods() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Set<String> skipTests = new HashSet<String>();
		skipTests.add("close"); 
		skipTests.add("getConnection"); 
		skipTests.add("isClosed");
		skipTests.add("internalClose");
		skipTests.add("trackResultSet");
		skipTests.add("checkClosed"); 
		skipTests.add("$VRi"); // this only comes into play when code coverage is started. Eclemma bug?

		CommonTestUtils.testStatementBounceMethod(mockConnection, testClass, skipTests, mockClass);
		
	}
	

	/** Test other methods not covered by the standard methods above.
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testRemainingForCoverage() throws SQLException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException{
		Statement mockStatement = createNiceMock(Statement.class);
		IStatementCache mockCache = createNiceMock(IStatementCache.class);
		ResultSet mockResultSet = createNiceMock(ResultSet.class);

		// alternate constructor
		StatementHandle handle = new StatementHandle(mockStatement, null);

		handle = new StatementHandle(mockStatement, null, mockCache, null, "testSQL");
		handle.setLogicallyOpen();
		handle.getConnection();
		
		Field field = handle.getClass().getDeclaredField("logicallyClosed");
		field.setAccessible(true);
		field.set(handle, true);
		
		Method method = handle.getClass().getDeclaredMethod("checkClosed");
		method.setAccessible(true);
		try{
			method.invoke(handle);
			Assert.fail("Exception should have been thrown");
		} catch (Throwable e){
		 // do nothing	
		}
		
		
		mockStatement.close();
		expectLastCall();
		mockCache.clear();
		expectLastCall();
		replay(mockStatement, mockCache); 

		handle.internalClose();
		verify(mockStatement, mockCache);
		reset(mockStatement, mockCache);


		ConcurrentLinkedQueue<ResultSet> mockResultSetHandle = createNiceMock(ConcurrentLinkedQueue.class);
		field = handle.getClass().getDeclaredField("resultSetHandles");
		field.setAccessible(true);
		field.set(handle, mockResultSetHandle);
		
		
		expect(mockResultSetHandle.add((ResultSet)anyObject())).andReturn(true);
		
		replay(mockResultSetHandle, mockResultSet);
		method = handle.getClass().getDeclaredMethod("trackResultSet", ResultSet.class);
		method.setAccessible(true);
		method.invoke(handle, mockResultSet);
		
		verify(mockResultSetHandle, mockResultSet);
		
		reset(mockResultSetHandle, mockResultSet);
		handle.setLogicallyOpen();
		expect(mockResultSetHandle.poll()).andReturn(mockResultSet).once();
		mockResultSet.close();
		expectLastCall();
		replay(mockResultSet, mockResultSetHandle);
		handle.close();

		verify(mockResultSetHandle, mockResultSet);
				
		handle.setLogicallyOpen();
		Assert.assertFalse(handle.isClosed());
	}

}
