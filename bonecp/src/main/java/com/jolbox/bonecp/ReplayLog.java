/**
 * 
 */
package com.jolbox.bonecp;

import java.lang.reflect.Method;

/**
 * @author Wallace
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
