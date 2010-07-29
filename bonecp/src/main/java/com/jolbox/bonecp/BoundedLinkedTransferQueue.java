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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import jsr166y.LinkedTransferQueue;

/**
 * A bounded version of linkedTransferQueue.
 * @author wallacew
 *
 * @param <E>
 */
public class BoundedLinkedTransferQueue<E> extends LinkedTransferQueue<E>{
	/** UUID */
	private static final long serialVersionUID = -1875525368357897907L;
	/** No of elements in queue. */
	private AtomicInteger size = new AtomicInteger();
	/** bound of queue. */
	private final int maxQueueSize;
	/** Main lock guarding all access */
	private final ReentrantLock lock = new ReentrantLock();

	/** Constructor.
	 * @param maxQueueSize
	 */
	public BoundedLinkedTransferQueue(int maxQueueSize){
		this.maxQueueSize = maxQueueSize;
	}

	@Override
	public int size(){
		return this.size.get();
	}

	/**
	 * Returns the number of free slots in this queue. 
	 *
	 * @return number of free slots.
	 */
	@Override
	public int remainingCapacity(){
		return this.maxQueueSize - this.size.get();
	}

	@Override
	public E poll() {

		E result = super.poll();

		if (result != null){
			this.size.decrementAndGet();
		}

		return result;
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {

		E result = super.poll(timeout, unit);

		if (result != null){
			this.size.decrementAndGet();
		}

		return result;
	}

	@Override
	public boolean tryTransfer(E e) {
		boolean result = super.tryTransfer(e);
		if (result){
			this.size.incrementAndGet();
		}
		return result;
	}


	@Override
	/** Inserts the specified element at the tail of this queue. 
	 * Will return false if the queue limit has been reached. 
	 * 
	 */
	public boolean offer(E e){
		boolean result = false;
		this.lock.lock();
		try{
			if (this.size.get() < this.maxQueueSize){
				super.put(e);
				this.size.incrementAndGet();
				result = true;
			}
		} finally {
			this.lock.unlock();
		}
		return result;	
	}

	@Override
	public void put(E e){
		throw new UnsupportedOperationException(); // we don't offer blocking yet
	}
}
