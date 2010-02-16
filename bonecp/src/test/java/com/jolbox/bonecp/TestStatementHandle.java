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

		testClass = new StatementHandle(mockClass, "", mockCallableStatementCache, mockConnection, "testSQL", true);

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
		skipTests.add("closeAndClearResultSetHandles"); 
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
		StatementHandle handle = new StatementHandle(mockStatement, null, true);

		handle = new StatementHandle(mockStatement, null, mockCache, null, "testSQL", true);
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
		
		// expect(mockResultSetHandle.poll()).andReturn(mockResultSet).once();
		mockResultSet.close();
		expectLastCall();
		replay(mockResultSet, mockResultSetHandle);
		handle.close();

		verify(mockResultSetHandle); // , mockResultSet);
				
		handle.setLogicallyOpen();
		Assert.assertFalse(handle.isClosed());
		
		reset(mockCache);
		mockCache.clear();
		expectLastCall().once();
		replay(mockCache);
		handle.clearCache();
		verify(mockCache);
	}
	
	/** Clear handles test
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void clearResultSetHandles() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException{
		Statement mockStatement = createNiceMock(Statement.class);
		ResultSet mockResultSet = createNiceMock(ResultSet.class);
		ConcurrentLinkedQueue<ResultSet> mockResultSetHandles = createNiceMock(ConcurrentLinkedQueue.class);

		// alternate constructor
		StatementHandle handle = new StatementHandle(mockStatement, null, true);
		Field field = handle.getClass().getDeclaredField("resultSetHandles");
		field.setAccessible(true);
		field.set(handle, mockResultSetHandles);
		expect(mockResultSetHandles.poll()).andReturn(mockResultSet).once().andReturn(null).once();
		mockResultSet.close();
		expectLastCall().once();
		replay(mockResultSetHandles, mockResultSet, mockStatement);
		handle.closeAndClearResultSetHandles();
		verify(mockResultSet, mockResultSetHandles, mockStatement);
		
	}

}
