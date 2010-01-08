/**
 * Copyright 2009 Wallace Wadge
 *
 * This file is part of BoneCP.
 *
 * BoneCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BoneCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package com.jolbox.bonecp;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;


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
		mockLogger = createStrictMock(Logger.class);
		mockThread = createNiceMock(Thread.class);
		
		testClass = new CloseThreadMonitor(mockThread, mockConnection, "fakeexception");
		Field field = testClass.getClass().getDeclaredField("logger");
		field.setAccessible(true);
		field.set(testClass, mockLogger);
		
		
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
		
		expect(mockConnection.isLogicallyClosed()).andReturn(true).once();
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
		expect(mockConnection.isLogicallyClosed()).andReturn(false).once();
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
		
		expect(mockConnection.isLogicallyClosed()).andThrow(new RuntimeException()).once();
		replay(mockConnection, mockLogger);
		testClass.run();
		verify(mockConnection, mockLogger);
	}

}
