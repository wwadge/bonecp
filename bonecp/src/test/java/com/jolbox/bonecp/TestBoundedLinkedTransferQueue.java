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
	static BoundedLinkedTransferQueue<Object> testClass = new BoundedLinkedTransferQueue<Object>(50);
	
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
		assertEquals(40, testClass.remainingCapacity());
	}

	/**
	 * Tests try transfer
	 * @throws InterruptedException
	 */
	@Test
	public void testTryTransfer() throws InterruptedException {
		Thread t = new Thread(new Runnable() {
			
			@Override
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
		assertEquals(10, testClass.size());
		testClass.tryTransfer(new Object());
		assertEquals(11, testClass.size());
	}
	/**
	 * Tests offer.
	 */
	@Test
	public void testOffer() {
		Object o = new Object();
		testClass.offer(o);
		assertEquals(12, testClass.size());
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
		assertEquals(12, testClass.size());
		assertEquals(o, testClass.poll(1, TimeUnit.DAYS));
		assertEquals(11, testClass.size());
	}

	/**
	 * Tests poll.
	 */
	@Test
	public void testPoll() {
		testClass.clear();
		Object o = new Object();
		testClass.offer(o);
		assertEquals(12, testClass.size());
		assertEquals(o, testClass.poll());
		assertEquals(11, testClass.size());
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
