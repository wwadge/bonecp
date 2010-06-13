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
	private int acquireRetryDelay;
	/** Number of attempts left before giving up. */
	private AtomicInteger acquireRetryAttempts = new AtomicInteger();
	/** Message that shows the origin of the problem. */ 
	private String logMessage = "";
	/** An opaque handle to be used by your application if necessary. */
	private Object debugHandle;
	/** Getter for acquireRetryDelay. By default starts off with whatever is set in the config.
	 * @return the acquireRetryDelay value
	 */
	public int getAcquireRetryDelay() {
		return this.acquireRetryDelay;
	}
	
	/** Sets the new acquireRetryDelay. Does not affect the global config.
	 * @param acquireRetryDelay the acquireRetryDelay to set
	 */
	public void setAcquireRetryDelay(int acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
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
