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

import com.jolbox.bonecp.hooks.ConnectionHook;

/** MBean interface for config.
 * @author Wallace
 *
 */
public interface BoneCPConfigMBean {

	/**
	 * Gets the minimum number of connections that will be contained in every partition.
	 *
	 * @return minConnectionsPerPartition
	 */
	int getMinConnectionsPerPartition();

	/**
	 * Gets the maximum number of connections that will be contained in every partition.
	 *
	 * @return maxConnectionsPerPartition
	 */
	int getMaxConnectionsPerPartition();

	/**
	 * Gets the acquireIncrement property.
	 * 
	 * Gets the current value of the number of connections to add every time the number of available connections is 
	 * about to run out (up to the maxConnectionsPerPartition).
	 *
	 * @return acquireIncrement number of connections to add.
	 */
	int getAcquireIncrement();

	/**
	 * Gets the number of currently defined partitions.
	 *
	 * @return partitionCount
	 */
	int getPartitionCount();

	/**
	 * Gets the configured JDBC URL
	 *
	 * @return jdbcUrl
	 */
	String getJdbcUrl();

	/**
	 * Gets username to use for the connections.
	 *
	 * @return username
	 */
	String getUsername();

	/**
	 * Gets the currently set idleConnectionTestPeriod.
	 *
	 * @return idleConnectionTestPeriod
	 */
	long getIdleConnectionTestPeriod();

	/**
	 * Gets idleMaxAge (time in min).
	 *
	 * @return idleMaxAge
	 */
	long getIdleMaxAge();

	/**
	 * Gets connectionTestStatement
	 *
	 * @return connectionTestStatement
	 */
	String getConnectionTestStatement();

	/**
	 * Gets preparedStatementsCacheSize setting.
	 * 
	 * @return preparedStatementsCacheSize
	 */
	int getPreparedStatementsCacheSize();

	/**
	 * Gets number of release-connection helper threads to create per partition.
	 *
	 * @return number of threads 
	 */
	int getReleaseHelperThreads();

	/**
	 * Gets no of statements cached per connection.
	 *
	 * @return no of statements cached per connection.
	 */
	int getStatementsCachedPerConnection();

	/** Returns the connection hook class.
	 * @return the connectionHook
	 */
	ConnectionHook getConnectionHook();

	/** Returns the initSQL parameter.
	 * @return the initSQL
	 */
	String getInitSQL();

}