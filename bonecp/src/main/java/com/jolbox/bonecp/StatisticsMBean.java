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
package com.jolbox.bonecp;


/** MBean (JMX) contract.
 * @author Wallace
 *
 */
public interface StatisticsMBean {

	/** 
	 * Return the average time it takes for a getConnection request to be services (in ms)
	 * @return Time in ms
	 */
	long getWaitTimeAvg();

	/** Return total number of connections currently in use by an application
	 * @return no of leased connections
	 */
	int getTotalLeased();

	/** Return the number of free connections available to an application right away (excluding connections that can be
	 * created dynamically)
	 * @return number of free connections
	 */
	int getTotalFree();

	/**
	 * Return total number of connections created in all partitions.
	 *
	 * @return number of created connections
	 */
	int getTotalCreatedConnections();

	/**
	 * Returns the cacheHits field.
	 * @return cacheHits
	 */
	long getCacheHits();

	/**
	 * Returns the cacheMiss field.
	 * @return cacheMiss
	 */
	long getCacheMiss();

	/**
	 * Returns the statementsCached field.
	 * @return statementsCached
	 */
	long getStatementsCached();

	/**
	 * Returns the connectionsRequested field.
	 * @return connectionsRequested
	 */
	long getConnectionsRequested();

	/**
	 * Returns the connectionWaitTime field in ms.
	 * @return connectionWaitTime
	 */
	long getConnectionWaitTime();


	/**
	 * Reset all statistics.
	 */
	void resetStats();
}