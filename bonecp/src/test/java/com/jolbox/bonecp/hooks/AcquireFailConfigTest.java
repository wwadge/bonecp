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
