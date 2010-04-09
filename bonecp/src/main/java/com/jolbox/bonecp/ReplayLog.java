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
