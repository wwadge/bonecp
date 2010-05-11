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
//	NANOPOOL,
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
