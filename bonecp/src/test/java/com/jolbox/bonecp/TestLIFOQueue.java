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

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

/** 
 * Tests for the LIFO queue.
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
		TestLIFOQueue.testClass.tryTransfer(o, 9999, TimeUnit.SECONDS);
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
