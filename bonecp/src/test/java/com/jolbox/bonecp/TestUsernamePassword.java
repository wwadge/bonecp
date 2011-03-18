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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test for username/password
 * @author wallacew
 *
 */
public class TestUsernamePassword {

	/**
	 * coverage
	 */
	@Test
	public void testUsernamePassword(){
		UsernamePassword up = new UsernamePassword("foo", "bar");
		assertEquals("foo", up.getUsername());
		assertEquals("bar", up.getPassword());
		
		UsernamePassword up2 = new UsernamePassword("foo", "bar");
		UsernamePassword up3 = new UsernamePassword("foo", "bar2");
		assertTrue(up.equals( up2 ) );
		assertFalse(up.equals( up3 ) );
		
		
		UsernamePassword up4 = new UsernamePassword("foo", "bar");
		assertFalse(up4.equals(new String()));
		
		assertNotNull(up.hashCode());
	}
}
