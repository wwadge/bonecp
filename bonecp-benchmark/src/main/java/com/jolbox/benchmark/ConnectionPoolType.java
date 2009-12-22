/**
 * 
 */
package com.jolbox.benchmark;

/**
 * @author Wallace
 *
 */
public enum ConnectionPoolType {
	C3P0(false), 
//	NANOPOOL,
	DBCP(false), 
	BONECP(true);
	
	private boolean enabled;

	private ConnectionPoolType(boolean enabled){
		this.enabled = enabled;
	}

	protected boolean isEnabled() {
		return enabled;
	}

	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}
