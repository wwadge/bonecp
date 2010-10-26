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

/* #ifdef JDK5
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
#endif JDK5 */

import jsr166y.TransferQueue;

// #ifdef JDK6
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

// #endif JDK6 


/**
 * An implementation that uses LinkedBlockingDeque internally to hide the difference between
 * this version and a BoundedLinkedTransferQueue (when used in FIFO mode).
 * 
 * This changes the queue so that every insert function is inserted at the head of the list.
 * @author wallacew
 * @param <E> 
 *
 */
// #ifdef JDK6
@SuppressWarnings("all")
public class LIFOQueue<E> extends LinkedBlockingDeque<E> implements TransferQueue<E>{

	
	private static final long serialVersionUID = -3503791017846313243L;

	public LIFOQueue(int capacity) {
		super(capacity);
	}

	public LIFOQueue() {
		super();
	}

	@Override
	public boolean tryTransfer(E e) {
		return super.offerFirst(e);
	}

	@Override
	public void transfer(E e) throws InterruptedException {
		putFirst(e);
	}

	@Override
	public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return offerFirst(e, timeout, unit);
	}

	@Override
	public boolean hasWaitingConsumer() {
		return false;
	}

	@Override
	public int getWaitingConsumerCount() {
		return 0;
	}

	
	@Override
	public boolean offer(E e) {
		return super.offerFirst(e);
	}

}
// #endif JDK6

/* #ifdef JDK5
// for JDK5, there's no dequeue implementation so we fall back on a simple linkedblockingqueue
public class LIFOQueue<E> extends LinkedBlockingQueue<E> implements TransferQueue<E>{

	private static final long serialVersionUID = -3503791017846313243L;

	public LIFOQueue(int capacity) {
		super(capacity);
	}

	public LIFOQueue() {
		super();
	}

	public boolean tryTransfer(E e) {
		return super.add(e);
	}

	public void transfer(E e) throws InterruptedException {
		super.put(e);
	}

	public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return super.offer(e, timeout, unit);
	}

	public boolean hasWaitingConsumer() {
		return false;
	}

	public int getWaitingConsumerCount() {
		return 0;
	}

	
	public boolean offer(E e) {
		return super.offer(e);
	}


}
#endif JDK5 */