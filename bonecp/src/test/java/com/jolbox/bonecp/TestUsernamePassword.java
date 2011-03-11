package com.jolbox.bonecp;

import static org.junit.Assert.assertEquals;

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
		assertEquals(up, up2);
		
		
	}
}
