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

package com.jolbox.bonecp.hooks;

import java.util.Map;

import com.jolbox.bonecp.ConnectionHandle;

/** JUnit helper.
 * @author wallacew
 *
 */
public class CustomHook extends AbstractConnectionHook{
    /** junit helper.*/
	public int acquire;
	/** junit helper.*/
	public int checkin;
	/** junit helper.*/
	public int checkout;
	/** junit helper.*/
	public volatile int destroy;
	/** junit helper.*/
	public int fail;
	/** junit helper.*/
	public int queryTimeout;


	@Override
	public synchronized void onAcquire(ConnectionHandle connection) {
		this.acquire++;
	}

	@Override
	public synchronized void onCheckIn(ConnectionHandle connection) {
		this.checkin++;
	}

	@Override
	public synchronized void onCheckOut(ConnectionHandle connection) {
		this.checkout++;
	}
	
	@Override
	public synchronized void onDestroy(ConnectionHandle connection) {
		this.destroy++;
	}

	@Override
	public synchronized boolean onAcquireFail(Throwable t, AcquireFailConfig acquireConfig) {
		this.fail++;
		if (this.fail < 3){
			return true; // try 3 times
		} 
		return false;

	}
	
	@Override
	public synchronized void onQueryExecuteTimeLimitExceeded(String sql, Map<Object, Object> logParams){
		this.queryTimeout++;
	}
}