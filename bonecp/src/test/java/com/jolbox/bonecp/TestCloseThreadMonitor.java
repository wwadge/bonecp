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

/**
 * 
 */
package com.jolbox.bonecp;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import static org.easymock.EasyMock.*;


/** Test for CloseThreadMonitor class
 * @author Wallace
 *
 */
public class TestCloseThreadMonitor {
	/** Mock handle. */
	private static ConnectionHandle mockConnection;
	/** Mock handle. */
	private static Logger mockLogger;
	/** Mock handle. */
	private static Thread mockThread;
	/** Class under test. */
	private static CloseThreadMonitor testClass;
 
	/**
	 * Test setup
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@BeforeClass
	public static void setup() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockThread = createNiceMock(Thread.class);
		testClass = new CloseThreadMonitor(mockThread, mockConnection, "fakeexception", 0);
    mockLogger = TestUtils.mockLogger(testClass.getClass());
	}
	
	/**
	 * Reset mocks.
	 */
	@Before
	public void before(){
		reset(mockConnection, mockLogger, mockThread);
	}
	
	/** Tests the normal case.
	 * @throws InterruptedException
	 */
	@Test
	public void testConnectionCorrectlyClosed() throws InterruptedException{
		
		mockThread.join();
	//	expectLastCall().once();
		
		expect(mockConnection.isClosed()).andReturn(true).once();
		replay(mockConnection, mockLogger, mockThread);
		testClass.run();
		verify(mockConnection, mockLogger, mockThread);
	}
	
	/** Test case where the connection is not closed.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testConnectionNotClosed() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		mockLogger.error((String)anyObject());
		expectLastCall().once();
		expect(mockConnection.isClosed()).andReturn(false).once();
		expect(mockConnection.getThreadUsingConnection()).andReturn(mockThread).once();
		replay(mockConnection, mockLogger);
		testClass.run();
		verify(mockConnection, mockLogger);
	}

	/** Code coverage.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InterruptedException
	 */
	@Test
	public void testConnectionInterrupted() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException{
		
		expect(mockConnection.isClosed()).andThrow(new RuntimeException()).once();
		replay(mockConnection, mockLogger);
		testClass.run();
		verify(mockConnection, mockLogger);
	}

}
