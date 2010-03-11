/**
 * 
 */
package com.jolbox.bonecp;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Wallace
 *
 */
public class TestReplayLog {

	/**
	 * Test method for {@link com.jolbox.bonecp.ReplayLog#ReplayLog(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
	 */
	@Test
	public void testAllGettersAndSetters() {
		ReplayLog testClass = new ReplayLog(new Object(), this.getClass().getMethods()[0], new Object[]{String.class});
		Object[] args = new Object[]{Integer.class};
		testClass.setArgs(args);
		assertTrue(args.equals(testClass.getArgs()));
		
		testClass.setMethod(this.getClass().getMethods()[0]);
		assertEquals(this.getClass().getMethods()[0], testClass.getMethod());
		
		Object obj = new Object();
		testClass.setTarget(obj);
		assertEquals(obj, testClass.getTarget());
		
		testClass.toString();
		testClass.setArgs(null);
		testClass.setTarget(null);
		testClass.setMethod(null);
		testClass.toString();
	}

}
