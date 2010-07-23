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

package com.jolbox.bonecp;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	public boolean offer(E e){
		boolean result = false;
			if (this.size.get() < this.maxQueueSize){
				super.put(e);
				this.size.incrementAndGet();
				result = true;
			}
		return result;	
	}

}
