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

package com.jolbox.bonecp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.util.PropertiesHelper;

import com.jolbox.bonecp.hooks.ConnectionHook;


/**
 * Hibernate Connection Provider.
 *
 * @author wallacew
 * @version $Revision$
 */
public class BoneCPConnectionProvider implements ConnectionProvider {
	/** Config key. */
	protected static final String CONFIG_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_PASSWORD = "hibernate.connection.password";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_USERNAME = "hibernate.connection.username";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_URL = "hibernate.connection.url";
	/** Config key. */
	protected static final String CONFIG_IDLE_MAX_AGE = "bonecp.idleMaxAge";
	/** Config stuff. */
	protected static final String CONFIG_CONNECTION_HOOK_CLASS = "bonecp.connectionHookClass";
	/** Config key. */
	protected static final String CONFIG_IDLE_CONNECTION_TEST_PERIOD = "bonecp.idleConnectionTestPeriod";
	/** Config key. */
	protected static final String CONFIG_RELEASE_HELPER_THREADS = "bonecp.releaseHelperThreads";
	/** Config key. */
	protected static final String CONFIG_PARTITION_COUNT = "bonecp.partitionCount";
	/** Config key. */
	protected static final String CONFIG_ACQUIRE_INCREMENT = "bonecp.acquireIncrement";
	/** Config key. */
	protected static final String CONFIG_MAX_CONNECTIONS_PER_PARTITION = "bonecp.maxConnectionsPerPartition";
	/** Config key. */
	protected static final String CONFIG_MIN_CONNECTIONS_PER_PARTITION = "bonecp.minConnectionsPerPartition";
	/** Config key. */
	protected static final String CONFIG_STATEMENTS_CACHED_PER_CONNECTION = "bonecp.statementsCachedPerConnection";
	/** Config key. */
	protected static final String CONFIG_PREPARED_STATEMENT_CACHE_SIZE = "bonecp.preparedStatementCacheSize";
	/** Config key. */
	protected static final String CONFIG_TEST_STATEMENT = "bonecp.connectionTestStatement";
	/** Config stuff. */
	private static final String CONFIG_STATUS = "Connection pool: URL = %s, username=%s, Min = %d, Max = %d, Acquire Increment = %d, Partitions = %d, idleConnection=%d, Max Age=%d";
	/** Connection pool handle. */
	private BoneCP pool;
	/** Isolation level. */
	private Integer isolation;
	/** Autocommit option. */
	private boolean autocommit;
	/** Configuration handle. */
	private BoneCPConfig config;
	/** Class logger. */
	private static Logger logger = Logger.getLogger(BoneCPConnectionProvider.class);

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#close()
	 */
	public void close()
	throws HibernateException {
		this.pool.shutdown();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#closeConnection(java.sql.Connection)
	 */
	public void closeConnection(Connection conn)
	throws SQLException {
		this.pool.releaseConnection(conn);
	} 

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#configure(java.util.Properties)
	 */
	public void configure(Properties props)
	throws HibernateException {
		String connectionTestStatement = props.getProperty(CONFIG_TEST_STATEMENT);

		int preparedStatementCacheSize = configParseNumber(props, CONFIG_PREPARED_STATEMENT_CACHE_SIZE, 50);
		int statementsCachedPerConnection = configParseNumber(props, CONFIG_STATEMENTS_CACHED_PER_CONNECTION, 30);
		int minsize = configParseNumber(props, CONFIG_MIN_CONNECTIONS_PER_PARTITION, 20);
		int maxsize = configParseNumber(props, CONFIG_MAX_CONNECTIONS_PER_PARTITION, 50);
		int acquireIncrement = configParseNumber(props, CONFIG_ACQUIRE_INCREMENT, 10);
		int partcount = configParseNumber(props, CONFIG_PARTITION_COUNT, 3);
		int releaseHelperThreads = configParseNumber(props, CONFIG_RELEASE_HELPER_THREADS, 3);
		long idleMaxAge = configParseNumber(props, CONFIG_IDLE_MAX_AGE, 240);
		long idleConnectionTestPeriod = configParseNumber(props, CONFIG_IDLE_CONNECTION_TEST_PERIOD, 60);

		String url = props.getProperty(CONFIG_CONNECTION_URL, "JDBC URL NOT SET IN CONFIG");
		String username = props.getProperty(CONFIG_CONNECTION_USERNAME, "username not set");
		String password = props.getProperty(CONFIG_CONNECTION_PASSWORD, "password not set");
		String connectionHookClass = props.getProperty(CONFIG_CONNECTION_HOOK_CLASS);


		// Remember Isolation level
		this.isolation = PropertiesHelper.getInteger(Environment.ISOLATION, props);
		this.autocommit = PropertiesHelper.getBoolean(Environment.AUTOCOMMIT, props);

		try {
			Class.forName(props.getProperty(CONFIG_CONNECTION_DRIVER_CLASS));
			logger.debug(String.format(CONFIG_STATUS, url, username, minsize, maxsize, acquireIncrement, partcount, idleConnectionTestPeriod/1000, idleMaxAge/(60*1000)));
			this.config = new BoneCPConfig();
			this.config.setMinConnectionsPerPartition(minsize);
			this.config.setMaxConnectionsPerPartition(maxsize);
			this.config.setAcquireIncrement(acquireIncrement);
			this.config.setPartitionCount(partcount);
			this.config.setJdbcUrl(url);
			this.config.setUsername(username);
			this.config.setPassword(password);
			this.config.setReleaseHelperThreads(releaseHelperThreads);
			this.config.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
			this.config.setIdleMaxAge(idleMaxAge);
			this.config.setConnectionTestStatement(connectionTestStatement);
			this.config.setPreparedStatementsCacheSize(preparedStatementCacheSize);
			this.config.setStatementsCachedPerConnection(statementsCachedPerConnection);
			
			if (connectionHookClass != null){
				Object hookClass = Class.forName(connectionHookClass).newInstance();
				this.config.setConnectionHook((ConnectionHook) hookClass);
			}
			// create the connection pool
			this.pool = createPool(this.config);
		}
		catch (NullPointerException e) {
			throw new HibernateException(e);
		} catch (Exception e) {
			throw new HibernateException(e);
		} 
	}

	/** Creates the given connection pool with the given configuration. Extracted here to make unit mocking easier.
	 * @param config configuration object.
	 * @return BoneCP connection pool handle.
	 */
	protected BoneCP createPool(BoneCPConfig config) {
		try{ 
			return new BoneCP(config);
		}  catch (SQLException e) {
			throw new HibernateException(e);
		}
	}

	/** Returns the value of the given property.
	 * @param props Properties handle
	 * @param propertyName property to read in
	 * @param defaultValue value to return on no value being set (or error)
	 * @return the number read in from the properties, or default value on error/no value set
	 */
	private int configParseNumber(Properties props, String propertyName, int defaultValue) {
		int result = defaultValue;
		String val = props.getProperty(propertyName);
		if (val != null) {
			try{
				result = Integer.parseInt(val);
			} catch (NumberFormatException e){
				// do nothing, use the default value
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#getConnection()
	 */
	public Connection getConnection()
	throws SQLException {
		Connection connection = this.pool.getConnection();

		// set the Transaction Isolation if defined
		if (this.isolation !=null){
			connection.setTransactionIsolation( this.isolation.intValue() );
		}

		// toggle autoCommit to false if set
		if ( connection.getAutoCommit() != this.autocommit ){
			connection.setAutoCommit(this.autocommit);
		}
		return connection;


	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#supportsAggressiveRelease()
	 */
	public boolean supportsAggressiveRelease() {
		return false;
	}

	/** Returns the configuration object being used.
	 * @return configuration object
	 */
	protected BoneCPConfig getConfig() {
		return this.config;
	}

}
