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
