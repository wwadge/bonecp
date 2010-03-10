/**
 * 
 */
package com.jolbox.bonecp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Wallace
 *
 */
public class RecoveryResult {
	private Object result;
	private Map<Object, Object> replaceTarget = new HashMap<Object, Object>();
	/**
	 * @return the result
	 */
	protected Object getResult() {
		return this.result;
	}
	/**
	 * @param result the result to set
	 */
	protected void setResult(Object result) {
		this.result = result;
	}
	/**
	 * @return the replaceTarget
	 */
	protected Map<Object, Object> getReplaceTarget() {
		return this.replaceTarget;
	}
	/**
	 * @param replaceTarget the replaceTarget to set
	 */
	protected void setReplaceTarget(Map<Object, Object> replaceTarget) {
		this.replaceTarget = replaceTarget;
	}
}
