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
