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
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * Tests the bounded linked transfer queue implementation.
 * @author wallacew
 *
 */
public class TestBoundedLinkedTransferQueue {

	/**
	 * Handle to test class.
	 */
	private final BoundedLinkedTransferQueue<Object> testClass = new BoundedLinkedTransferQueue<Object>(50);
	
	/** Tests size param.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testSize() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		Field field = testClass.getClass().getDeclaredField("size");
		field.setAccessible(true);
		field.set(testClass, new AtomicInteger(10));
		assertEquals(10, testClass.size());
	}
	
	

	/**
	 * Test remainingCapacity 
	 */
	@Test
	public void testRemainingCapacity() {
		testClass.offer(new Object());
		assertEquals(49, testClass.remainingCapacity());
	}

	/**
	 * Tests try transfer
	 * @throws InterruptedException
	 */
	@Test
	public void testTryTransfer() throws InterruptedException {
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				try {
					testClass.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		while (!testClass.hasWaitingConsumer()){
			Thread.sleep(50); // wait till take() has been registered
		}
		assertEquals(0, testClass.size());
		testClass.tryTransfer(new Object());
		assertEquals(1, testClass.size());
	}
	/**
	 * Tests offer.
	 */
	@Test
	public void testOffer() {
		Object o = new Object();
		testClass.offer(o);
		assertEquals(1, testClass.size());
	}

	
	/**
	 * Tests poll with timeout
	 * @throws InterruptedException
	 */
	@Test
	public void testPollLongTimeUnit() throws InterruptedException {
		testClass.clear();
		Object o = new Object();
		testClass.offer(o);
		assertEquals(1, testClass.size());
		assertEquals(o, testClass.poll(9999, TimeUnit.SECONDS));
		assertEquals(0, testClass.size());
	}

	/**
	 * Tests poll.
	 */
	@Test
	public void testPoll() {
		testClass.clear();
		Object o = new Object();
		testClass.offer(o);
		assertEquals(1, testClass.size());
		assertEquals(o, testClass.poll());
		assertEquals(0, testClass.size());
	}

	/**
	 * Tests put.
	 */
	@Test
	public void put(){
		try{
			testClass.put(new Object());
			fail("Should have thrown an exception");
		} catch (Exception e){
			// do nothing
		}
	}
	
	/**
	 * Tests offer lock coverage.
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testOfferCoverage() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field field = testClass.getClass().getDeclaredField("size");
		field.setAccessible(true);
		field.set(testClass, null);
		
		Object o = new Object();
		try{
			testClass.offer(o);
		} catch (NullPointerException e){
			// do nothing
		}
	}
}
