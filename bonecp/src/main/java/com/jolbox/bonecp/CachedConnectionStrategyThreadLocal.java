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
/**
 * 
 */
package com.jolbox.bonecp;

import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * This is moved here to aid testing by exposing a dumbGet() method.
 * @author wwadge
 *
 */
public class CachedConnectionStrategyThreadLocal<T> extends	ThreadLocal<SimpleEntry<ConnectionHandle, Boolean>> {

	private ConnectionStrategy fallbackStrategy;
	private CachedConnectionStrategy ccs;

	public CachedConnectionStrategyThreadLocal(CachedConnectionStrategy ccs, ConnectionStrategy fallbackStrategy){
		this.fallbackStrategy = fallbackStrategy;
		this.ccs = ccs;
	}

	@Override
	protected SimpleEntry<ConnectionHandle, Boolean> initialValue() {
		SimpleEntry<ConnectionHandle, Boolean> result = null;
		ConnectionHandle c = null;
		// grab a connection from any other configured fallback strategy
		for (int i=0; i < 4*3; i++){	// there is a slight race with the pool watch creation thread so spin for a bit
			// no big deal if it keeps failing, we just switch to default connection strategy
			c = (ConnectionHandle)this.fallbackStrategy.pollConnection();
			if (c != null){
				break;
			}

			// oh-huh? either we are racing while resizing the pool or else no of threads > no of connections. Let's wait a bit 
			// and try again one last time
			Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
		}


		if (c != null){
			result = new SimpleEntry<ConnectionHandle, Boolean>(c, false);
			this.ccs.threadWatch(c);
		}

		return result;
	}


	public SimpleEntry<ConnectionHandle, Boolean> dumbGet(){
		return super.get();
	}

	@Override
	public SimpleEntry<ConnectionHandle, Boolean> get() {
		SimpleEntry<ConnectionHandle, Boolean> result = super.get();
		// have we got one that's cached and unused? Mark it as in use. 
		if (result == null || result.getValue()){
			// ... otherwise grab a new connection 
			ConnectionHandle fallbackConnection = (ConnectionHandle)this.fallbackStrategy.pollConnection();
			if (fallbackConnection == null){
				return null;
			}
			result = new SimpleEntry<ConnectionHandle, Boolean>(fallbackConnection, false);
		} 

		if (result != null){
			result.setValue(true);
		}

		result.getKey().logicallyClosed.set(false);
		return result;
	}
}
