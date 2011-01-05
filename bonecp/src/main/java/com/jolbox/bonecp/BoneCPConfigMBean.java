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

	/** Returns the name of the pool for JMX and thread names.
	 * @return a pool name.
	 */
	String getPoolName();
	
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
	 * Gets the currently set idleConnectionTestPeriodInMinutes.
	 *
	 * @return idleConnectionTestPeriod
	 */
	long getIdleConnectionTestPeriodInMinutes();

	/**
	 * Gets idleMaxAge (time in min).
	 *
	 * @return idleMaxAge
	 */
	long getIdleMaxAgeInMinutes();

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

	/** Returns true if SQL logging is currently enabled, false otherwise.
	 * @return the logStatementsEnabled status
	 */
	boolean isLogStatementsEnabled();

	/** Returns the number of ms to wait before attempting to obtain a connection again after a failure. Default: 7000.
	 * @return the acquireRetryDelay
	 */
	int getAcquireRetryDelayInMs();
	
	/** Returns true if connection pool is to be initialized lazily.
	 * @return lazyInit setting 
	 */
	boolean isLazyInit();

	/** Returns true if the pool is configured to record all transaction activity and replay the transaction automatically in case
	 * of connection failures.
	 * @return the transactionRecoveryEnabled status
	 */
	boolean isTransactionRecoveryEnabled();
	
	/** After attempting to acquire a connection and failing, try to connect these many times before giving up. Default 5. 
	 * @return the acquireRetryAttempts value
	 */
	int getAcquireRetryAttempts();
	
	/** Returns the connection hook class name as passed via the setter
	 * @return the connectionHookClassName.
	 */
	String getConnectionHookClassName();
	
	/** Return true if JMX is disabled.
	 * @return the disableJMX.
	 */
	boolean isDisableJMX();

	/** Return the query execute time limit in ms.
	 * @return the queryTimeLimit
	 */
	long getQueryExecuteTimeLimitInMs();
	
	/** Returns the pool watch connection threshold value.
	 * @return the poolAvailabilityThreshold currently set.
	 */
	int getPoolAvailabilityThreshold();
	
	/** Returns true if connection tracking has been disabled.
	 * @return the disableConnectionTracking
	 */
	boolean isDisableConnectionTracking();

	/** Returns the maximum time (in milliseconds) to wait before a call to getConnection is timed out.
	 * @return the connectionTimeout
	 */
	long getConnectionTimeoutInMs();
	
	/** Returns the no of ms to wait when close connection watch threads are enabled. 0 = wait forever.
	 * @return the watchTimeout currently set.
	 */
	long getCloseConnectionWatchTimeout();
	
	/**
	 * Returns the statementHelperThreads field.
	 * @return statementHelperThreads
	 */
	int getStatementReleaseHelperThreads();
	
	/**
	 * Returns the maxConnectionAge field.
	 * @return maxConnectionAge
	 */
	long getMaxConnectionAge();
	
	/**
	 * Returns the configFile field.
	 * @return configFile
	 */
	String getConfigFile();
	
	/**
	 * Returns the serviceOrder field.
	 * @return serviceOrder
	 */
	String getServiceOrder();
	
	/**
	 * Returns the statisticsEnabled field.
	 * @return statisticsEnabled
	 */
	boolean isStatisticsEnabled();
}