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
	/** Setter for map.
	 * @param replaceTarget the replaceTarget to set
	 */
	public void setReplaceTarget(Map<Object, Object> replaceTarget) {
		this.replaceTarget = replaceTarget;
	}
}
