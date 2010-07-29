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
	 * Gets statementsCacheSize setting.
	 * 
	 * @return statementsCacheSize
	 */
	int getStatementsCacheSize();

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