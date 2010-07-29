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

import java.lang.reflect.Method;

/**
 * @author wallacew
 *
 */
public class ReplayLog {
	/** Connection or statement. */
	private Object target;
	/** Method recorded. */
	private Method method;
	/** Arguments passed to method. */
	private Object[] args;
	
	/**
	 * @param target
	 * @param method
	 * @param args
	 */
	public ReplayLog(Object target, Method method, Object[] args) {
		this.target = target;
		this.method = method;
		this.args = args;
	}
	/**
	 * @return the method
	 */
	public Method getMethod() {
		return this.method;
	}
	/**
	 * @param method the method to set
	 */
	public void setMethod(Method method) {
		this.method = method;
	}
	
	/**
	 * @return the args
	 */
	public Object[] getArgs() {
		return this.args;
	}
	/**
	 * @param args the args to set
	 */
	public void setArgs(Object[] args) {
		this.args = args;
	}
	/**
	 * @return the target
	 */
	public Object getTarget() {
		return this.target;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(Object target) {
		this.target = target;
	}
	
	/** {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (this.target == null ? "" : this.target.getClass().getName())+"."
		+ (this.method == null ? "" : this.method.getName()) + " with args "
		+ (this.args == null ? "null" : this.args);
	}
	
	
}
