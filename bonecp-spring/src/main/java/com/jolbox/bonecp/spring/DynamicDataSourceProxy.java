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

package com.jolbox.bonecp.spring;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

/** Like a normal datasource except it supports an extra method: switchDataSource to slowly migrate to a new datasource.
 * 
 * Simply call switchDataSource with your new BoneCP configuration: the existing pool will be shutdown gracefully (checkout out
 * connections are unaffected) and a new one will take it's place. A typical use case would be to transparently instruct your application
 * to use a new database without restarting the application.
 *   
 * @author Wallace
 *
 */
public class DynamicDataSourceProxy extends DelegatingDataSource{
	/** Logging. */
	private static final Log logger = LogFactory.getLog(LazyConnectionDataSourceProxy.class);
	
	/**
	 * Create a new DynamicDataSourceProxy.
	 * @param targetDataSource the target DataSource
	 */
	public DynamicDataSourceProxy(DataSource targetDataSource) {
		setTargetDataSource(targetDataSource);
		afterPropertiesSet();
	}
	
	/**
	 * Default constructor.
	 */
	public DynamicDataSourceProxy(){
		// default constructor
	}
	
	/** Switch to a new DataSource using the given configuration.
	 * @param newConfig BoneCP DataSource to use.
	 * @throws SQLException
	 */
	public void switchDataSource(BoneCPConfig newConfig) throws SQLException {
		logger.info("Switch to new datasource requested. New Config: "+newConfig);
		DataSource oldDS = getTargetDataSource();
 
		if (!(oldDS instanceof BoneCPDataSource)){
			throw new SQLException("Unknown datasource type! Was expecting BoneCPDataSource but received "+oldDS.getClass()+". Not switching datasource!");
		}
		
		BoneCPDataSource newDS = new BoneCPDataSource(newConfig);
		newDS.getConnection().close(); // initialize a connection (+ throw it away) to force the datasource to initialize the pool
		
		// force application to start using the new one 
		setTargetDataSource(newDS);
		
		logger.info("Shutting down old datasource slowly. Old Config: "+oldDS);
		// tell the old datasource to terminate. This terminates the pool lazily so existing checked out connections can still be used.
		((BoneCPDataSource)oldDS).close();
	}
}
