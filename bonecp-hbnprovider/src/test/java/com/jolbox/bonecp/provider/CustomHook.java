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

package com.jolbox.bonecp.provider;

import java.util.Map;

import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.hooks.AbstractConnectionHook;

/** JUnit helper.
 * @author wallacew
 *
 */
public class CustomHook extends AbstractConnectionHook{
    /** junit helper.*/
	int acquire;
	/** junit helper.*/
	int checkin;
	/** junit helper.*/
	int checkout;
	/** junit helper.*/
	int destroy;
	/** junit helper.*/
	private int queryTimeout;

	@Override
	public void onAcquire(ConnectionHandle connection) {
		this.acquire++;
	}

	@Override
	public void onCheckIn(ConnectionHandle connection) {
		this.checkin++;
	}

	@Override
	public void onCheckOut(ConnectionHandle connection) {
		this.checkout++;
	}
	
	@Override
	public void onDestroy(ConnectionHandle connection) {
		this.destroy++;
	}

	@Override
	public void onQueryExecuteTimeLimitExceeded(String sql, Map<Object, Object> logParams){
		this.queryTimeout++;
	}
}
