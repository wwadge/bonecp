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


import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.hooks.CoverageHook;
import com.jolbox.bonecp.hooks.CustomHook;
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
	/** Mock handle. */
	private static BoneCPConfig mockConfig;
	/** Mock handle. */
	private static BoneCP mockPool;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mockClass = createNiceMock(PreparedStatementHandle.class);
		mockCallableStatementCache = createNiceMock(IStatementCache.class);
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockConfig = createNiceMock(BoneCPConfig.class);
		mockPool = createNiceMock(BoneCP.class);
		expect(mockConnection.getPool()).andReturn(mockPool).anyTimes();
		expect(mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		expect(mockConfig.getQueryExecuteTimeLimit()).andReturn(1).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(new CoverageHook()).anyTimes();
		replay(mockConnection, mockPool, mockConfig);
		testClass = new StatementHandle(mockClass, "", mockCallableStatementCache, mockConnection, "testSQL", true);
		reset(mockConnection, mockPool, mockConfig);
		

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		reset(mockClass, mockCallableStatementCache, mockConnection, mockPool);
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
		skipTests.add("closeStatement"); 
		skipTests.add("closeAndClearResultSetHandles"); 
		skipTests.add("$VRi"); // this only comes into play when code coverage is started. Eclemma bug?

		CommonTestUtils.testStatementBounceMethod(null, mockConnection, testClass, skipTests, mockClass);
		
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
	@Test
	public void testRemainingForCoverage() throws SQLException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException{
		Statement mockStatement = createNiceMock(Statement.class);
		IStatementCache mockCache = createNiceMock(IStatementCache.class);
		expect(mockConnection.getPool()).andReturn(mockPool).anyTimes();
		expect(mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		expect(mockConfig.getQueryExecuteTimeLimit()).andReturn(1).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(new CoverageHook()).anyTimes();

		replay(mockConnection, mockConfig, mockPool);
		// alternate constructor 
		StatementHandle handle = new StatementHandle(mockStatement, mockConnection, true);

		handle = new StatementHandle(mockStatement, null, mockCache, mockConnection, "testSQL", true);
		
		handle.setLogicallyOpen();
		handle.getConnection();
		
		handle.logicallyClosed.set(true);
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
		verify(mockStatement);
		reset(mockStatement, mockCache);


		handle.setLogicallyOpen();
		Assert.assertFalse(handle.isClosed());
		
		reset(mockCache);
		mockCache.clear();
		expectLastCall().once();
		replay(mockCache);
		handle.clearCache();
		verify(mockCache);
		
		handle.setOpenStackTrace("foo");
		assertEquals("foo", handle.getOpenStackTrace());
		handle.sql = "foo";
		assertNotNull(handle.toString());
	}
	
	/** temp */
	boolean firstTime = true;
	
	/** Tests query time limit.
	 * @throws SQLException
	 */
	@Test
	public void testQueryTimeLimit() throws SQLException{
		Statement mockStatement = createNiceMock(Statement.class);
		// alternate constructor 
		expect(mockConnection.getPool()).andReturn(mockPool).anyTimes();
		expect(mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		reset(mockConfig);
		ConnectionPartition mockPartition = createNiceMock(ConnectionPartition.class);
		expect(mockPartition.getPreComputedQueryExecuteTimeLimit()).andReturn(1L).anyTimes();
		expect(mockConnection.getOriginatingPartition()).andReturn(mockPartition).anyTimes();
//		expect(mockConfig.getQueryExecuteTimeLimit()).andReturn(1).anyTimes();
		CustomHook hook = new CustomHook();
		expect(mockConfig.getConnectionHook()).andReturn(hook).anyTimes();
		mockStatement.execute((String)EasyMock.anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			
			@SuppressWarnings("unqualified-field-access")
			public Object answer() throws Throwable {
				if (firstTime){
					Thread.sleep(1300);
					firstTime=false;
				}
				return true;
			}
		}).anyTimes();
		replay(mockConnection, mockConfig, mockPool, mockStatement, mockPartition);

		StatementHandle handle = new StatementHandle(mockStatement, mockConnection, true);
		handle.execute("test");
		assertEquals(1, hook.queryTimeout);
	}

	/**
	 * Coverage.
	 */
	@Test
	public void testGetterSetter(){
		Statement mockStatement = createNiceMock(Statement.class); 
		testClass.setInternalStatement(mockStatement);
		Assert.assertEquals(mockStatement, testClass.getInternalStatement());
	}
}
