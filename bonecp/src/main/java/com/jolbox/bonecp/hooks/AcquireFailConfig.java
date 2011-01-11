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
package com.jolbox.bonecp.hooks;

import java.util.concurrent.atomic.AtomicInteger;

/** Parameter class passed to onAcquireFail hook.
 * @author Wallace
 *
 */
public class AcquireFailConfig {
	/** Delay to use between one acquire retry attempt and the next. */
	private long acquireRetryDelayInMs;
	/** Number of attempts left before giving up. */
	private AtomicInteger acquireRetryAttempts = new AtomicInteger();
	/** Message that shows the origin of the problem. */ 
	private String logMessage = "";
	/** An opaque handle to be used by your application if necessary. */
	private Object debugHandle;

	/** Deprecated. Use {@link #getAcquireRetryDelayInMs()} instead.
	 * @return the acquireRetryDelay value
	 * @deprecated Use {@link #getAcquireRetryDelayInMs()} instead.
	 */
	@Deprecated
	public long getAcquireRetryDelay() {
		return getAcquireRetryDelayInMs();
	}
	
	/** Getter for acquireRetryDelay. By default starts off with whatever is set in the config.
	 * @return the acquireRetryDelay value
	 */
	public long getAcquireRetryDelayInMs() {
		return this.acquireRetryDelayInMs;
	}
	/** Deprecated. Use {@link #setAcquireRetryDelayInMs(long)} instead.
	 * @param acquireRetryDelayInMs the acquireRetryDelay to set
	 * @deprecated Use {@link #setAcquireRetryDelayInMs(long)} instead.
	 */
	@Deprecated
	public void setAcquireRetryDelay(long acquireRetryDelayInMs) {
		setAcquireRetryDelayInMs(acquireRetryDelayInMs);
	}

	/** Sets the new acquireRetryDelay. Does not affect the global config.
	 * @param acquireRetryDelayInMs the acquireRetryDelay to set
	 */
	public void setAcquireRetryDelayInMs(long acquireRetryDelayInMs) {
		this.acquireRetryDelayInMs = acquireRetryDelayInMs;
	}

	/** Returns the acquireRetryAttemps. By default starts off with whatever is set in the config.
	 * @return the acquireRetryAttempts value.
	 */
	public AtomicInteger getAcquireRetryAttempts() {
		return this.acquireRetryAttempts;
	}
	
	/** Sets the new acquireRetyAttemps. 
	 * @param acquireRetryAttempts the acquireRetryAttempts to set
	 */
	public void setAcquireRetryAttempts(AtomicInteger acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	/** Returns a message that shows the origin of the problem. 
	 * @return the logMessage to display
	 */
	public String getLogMessage() {
		return this.logMessage;
	}

	/** Sets a log message.
	 * @param logMessage the logMessage to set
	 */
	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	/** Returns a reference to an opaque debug handle.
	 * @return the debugHandle.
	 */
	public Object getDebugHandle() {
		return this.debugHandle;
	}

	/** Sets a reference to an opaque debug reference.
	 * @param debugHandle the debugHandle to set
	 */
	public void setDebugHandle(Object debugHandle) {
		this.debugHandle = debugHandle;
	}
}
