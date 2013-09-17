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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.slf4j.Logger;

import static org.easymock.EasyMock.*;


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
		Logger mockLogger = TestUtils.mockLogger(testClass.getClass());
		makeThreadSafe(mockLogger, true);

		ExecutorService executor = Executors.newSingleThreadExecutor(testClass);
		mockLogger.error((String)anyObject(), (Throwable)anyObject());
		expectLastCall().once();
		replay(mockLogger);
		executor.execute(new MockThread());
		
		for (int i=0; i < 5; i++) {
				signalled = true;
			Thread.sleep(400);
		}
		verify(mockLogger);


	}
}
