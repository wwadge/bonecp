package com.jolbox.bonecp;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

/** 
 * Tests for the LIFO queue
 * @author wallacew
 *
 */
public class TestLIFOQueue {

	/** Handle. */
	private static LIFOQueue<Object> testClass;

	/**
	 * Class setup.
	 */
	@BeforeClass
	public static void setup(){
		testClass = new LIFOQueue<Object>(); // coverage
		testClass = new LIFOQueue<Object>(20);
	}
	
	

	/**
	 * Test for similarly named method.
	 */
	@Test
	public void testTryTransferE() {
		Object o = new Object();
		TestLIFOQueue.testClass.tryTransfer(o);
		assertEquals(o, TestLIFOQueue.testClass.poll());
	}

	/**
	 * Test for similarly named method.
	 * @throws InterruptedException 
	 */
	@Test
	public void testTransfer() throws InterruptedException {
		Object o = new Object();
		TestLIFOQueue.testClass.transfer(o);
		assertEquals(o, TestLIFOQueue.testClass.poll());
	}

	/**
	 * Test for similarly named method.
	 * @throws InterruptedException 
	 */
	@Test
	public void testTryTransferTimeUnit() throws InterruptedException {
		Object o = new Object();
		TestLIFOQueue.testClass.tryTransfer(o, 10, TimeUnit.DAYS);
		assertEquals(o, TestLIFOQueue.testClass.poll());
		
	}

	/**
	 * Test for similarly named method.
	 */
	@Test
	public void testHasWaitingConsumer() {
		assertEquals(false, TestLIFOQueue.testClass.hasWaitingConsumer());
	}

	/**
	 * Test for similarly named method.
	 */
	@Test
	public void testGetWaitingConsumerCount() {
		assertEquals(0, TestLIFOQueue.testClass.getWaitingConsumerCount());
	}

	/**
	 * Test for similarly named method.
	 */
	@Test
	public void testOfferE() {
		Object o = new Object();
		TestLIFOQueue.testClass.offer(o);
		assertEquals(o, TestLIFOQueue.testClass.poll());
	}

}
