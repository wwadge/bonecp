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
