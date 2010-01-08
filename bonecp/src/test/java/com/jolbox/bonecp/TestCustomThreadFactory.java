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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;


/**
 * @author wwadge
 *
 */
public class TestCustomThreadFactory {
	/** Thread signalling. */
	static volatile boolean signalled = false;

	/** Tests the uncaught exception handler. 
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InterruptedException
	 */
	@Test
	public void testUncaughtException() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException{
		CustomThreadFactory testClass = new CustomThreadFactory("test", false);
		Logger mockLogger = createNiceMock(Logger.class);
		makeThreadSafe(mockLogger, true);
		Field field = testClass.getClass().getDeclaredField("logger");
		field.setAccessible(true);
		field.set(testClass, mockLogger);

		ExecutorService executor = Executors.newSingleThreadExecutor(testClass);
		mockLogger.error((String)anyObject(), (Throwable)anyObject());
		expectLastCall().once();
		replay(mockLogger);
		executor.execute(new MockThread());
		
		for (int i=0; i < 5; i++) {
				signalled = true;
			Thread.sleep(100);
		}
		verify(mockLogger);


	}
}
