/**
 * 
 */
package com.jolbox.bonecp;


import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.reset;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * @author Wallace
 *
 */
public class TestCallableStatementHandle {
	/** Class under test. */
	private static CallableStatementHandle testClass;
	/** Mock class under test. */
	private static CallableStatementHandle mockClass;
	/** Mock handles. */
	private static IStatementCache mockCallableStatementCache;
	/** Mock handles. */
	private static ConnectionHandle mockConnection;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mockClass = createNiceMock(CallableStatementHandle.class);
		mockCallableStatementCache = createNiceMock(IStatementCache.class);
		mockConnection = createNiceMock(ConnectionHandle.class);
		testClass = new CallableStatementHandle(mockClass, "", mockCallableStatementCache, mockConnection, "somesql");

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
		skipTests.add("$VRi"); // this only comes into play when code coverage is started. Eclemma bug?
		CommonTestUtils.testStatementBounceMethod(mockConnection, testClass, skipTests, mockClass);
		
	}
}
