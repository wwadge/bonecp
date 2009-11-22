/**
 * 
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

import org.apache.log4j.Logger;
import org.junit.Test;

import com.jolbox.bonecp.CustomThreadFactory;


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
			Thread.sleep(500);
		}
		verify(mockLogger);


	}
}
