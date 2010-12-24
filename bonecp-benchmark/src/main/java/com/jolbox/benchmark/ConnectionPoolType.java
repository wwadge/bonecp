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
package com.jolbox.benchmark;

/**
 * @author Wallace
 *
 */
public enum ConnectionPoolType {
	/** pool type. */
	C3P0(false), 
	/** pool type. */
	PROXOOL(false),
	/** pool type. */
	DBPOOL(true),
	/** pool type. */
	DBCP(false),
	/** pool type. */
	TOMCAT_JDBC(false),
	/** pool type. */
	BONECP_1_PARTITIONS(true, false),
	/** pool type. */
	BONECP_2_PARTITIONS(false, true),
	/** pool type. */
	BONECP_4_PARTITIONS(false, true),
	/** pool type. */
	BONECP_5_PARTITIONS(false, true),
	/** pool type. */
	BONECP_10_PARTITIONS(false, true);
	/** inner state. */
	private boolean enabled;
	/** inner state. */
	private boolean multiPartitions;

	/**
	 * @param enabled
	 * @param multiPartitions
	 */
	private ConnectionPoolType(boolean enabled, boolean multiPartitions){
		this.enabled = enabled;
		this.multiPartitions = multiPartitions;
	}
	
	/**
	 * @param enabled
	 */
	private ConnectionPoolType(boolean enabled){
		this.enabled = enabled;
		this.multiPartitions = false;
	}

	/** 
	 * @return t/f
	 */
	protected boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @param enabled
	 */
	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	/**
	 * @return t/f
	 */
	protected boolean isMultiPartitions() {
		return this.multiPartitions;
	}
}
