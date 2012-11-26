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

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * This is moved here to aid testing by exposing a dumbGet() method.
 * @author wwadge
 *
 */
public class CachedConnectionStrategyThreadLocal<T> extends	ThreadLocal<ConnectionHandle> {

	private CachedConnectionStrategy ccs;

	public CachedConnectionStrategyThreadLocal(CachedConnectionStrategy ccs){
		this.ccs = ccs;
	}
	
	@Override
	protected ConnectionHandle initialValue() {
		ConnectionHandle result;
		try {
			// grab a connection from any other configured fallback strategy
			result = this.ccs.pollFallbackConnection();
			
			if (result == null){
				// oh-huh? either we are racing while resizing the pool or else no of threads > no of connections. Let's wait a bit 
				// and try again one last time
				for (int i=0; i < 4*3; i++){	// there is a slight race with the pool watch creation thread so spin for a bit
															// no big deal if it keeps failing, we just switch to default connection strategy
					result = this.ccs.pollFallbackConnection();
					if (result != null){
						break;
					}
					Uninterruptibles.sleepUninterruptibly(250, TimeUnit.MILLISECONDS);
				}
				
			}
		} catch (SQLException e) {
			result = null; 
		}
		return result;
	}


	public ConnectionHandle dumbGet(){
		return super.get();
	}

	@Override
	public ConnectionHandle get() {
		ConnectionHandle result = super.get();
		// have we got one that's cached and unused? Mark it as in use. 
		if (result == null || !result.inUseInThreadLocalContext.compareAndSet(false, true)){
			try {
				// ... otherwise grab a new connection 
				result = this.ccs.pollFallbackConnection();
			} catch (SQLException e) {
				result = null;
			}
		} 

		return result;
	}
}
