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
