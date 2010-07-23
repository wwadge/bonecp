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
