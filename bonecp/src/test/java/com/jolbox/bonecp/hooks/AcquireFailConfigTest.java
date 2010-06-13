/**
 * 
 */
package com.jolbox.bonecp.hooks;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;


/** Test for AcquireFailConfig class.
 * @author Wallace
 *
 */
public class AcquireFailConfigTest {
	
	/**
	 * Test getters/setters for acquireFail class.
	 */
	@Test
	public void testGettersSetters(){
		Object obj = new Object();
		AcquireFailConfig config = new AcquireFailConfig();
		config.setAcquireRetryAttempts(new AtomicInteger(1));
		config.setAcquireRetryDelay(123);
		config.setLogMessage("test");
		config.setDebugHandle(obj);

		assertEquals(1, config.getAcquireRetryAttempts().get());
		assertEquals(123, config.getAcquireRetryDelay());
		assertEquals("test", config.getLogMessage());
		assertEquals(obj, config.getDebugHandle());
		
	}

}
