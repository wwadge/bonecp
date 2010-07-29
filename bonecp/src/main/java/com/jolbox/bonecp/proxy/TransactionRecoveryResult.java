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
package com.jolbox.bonecp.proxy;

import java.util.HashMap;
import java.util.Map;

/** Used to return multiple values in transaction recovery mode.
 * @author Wallace
 *
 */
public class TransactionRecoveryResult {
	/** Final result obtained from playback of transaction. */
	private Object result;
	/** Mappings between old connections/statements to new ones. */
	private Map<Object, Object> replaceTarget = new HashMap<Object, Object>();
	
	/** Getter for result.
	 * @return the result
	 */
	public Object getResult() {
		return this.result;
	}
	/** Setter for result.
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}
	/** Getter for map.
	 * @return the replaceTarget
	 */
	public Map<Object, Object> getReplaceTarget() {
		return this.replaceTarget;
	}
}
