/**
 * 
 */
package com.jolbox.benchmark;

/**
 * @author Wallace
 *
 */
public enum ConnectionPoolType {
	C3P0(true), 
//	NANOPOOL,
	DBCP(true),
	BONECP_1_PARTITIONS(true, false),
	BONECP_2_PARTITIONS(true, true),
	BONECP_4_PARTITIONS(false, true),
	BONECP_5_PARTITIONS(true, true),
	BONECP_10_PARTITIONS(false, true);
	
	private boolean enabled;
	private boolean multiPartitions;

	private ConnectionPoolType(boolean enabled, boolean multiPartitions){
		this.enabled = enabled;
		this.multiPartitions = multiPartitions;
	}
	
	private ConnectionPoolType(boolean enabled){
		this.enabled = enabled;
		this.multiPartitions = false;
	}

	protected boolean isEnabled() {
		return enabled;
	}

	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	protected boolean isMultiPartitions() {
		return multiPartitions;
	}
}
