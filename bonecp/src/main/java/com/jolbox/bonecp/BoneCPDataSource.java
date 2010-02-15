/*

Copyright 2009 Wallace Wadge

This file is part of BoneCP.

BoneCP is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BoneCP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jolbox.bonecp;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.hooks.ConnectionHook;


/**
 * DataSource for use with LazyConnection Provider etc.
 *
 * @author wallacew
 * @version $Revision$
 */
public class BoneCPDataSource implements DataSource, Serializable{
	/** Serialization UID. */
	private static final long serialVersionUID = -1561804548443209469L;
	/** Config stuff. */
	private static final String CONFIG_STATUS = "Connection pool: URL = %s, username=%s, Min = %d, Max = %d, Acquire Increment = %d, Partitions = %d, idleConnection=%d, Max Age=%d";
	/** Config setting. */
	private PrintWriter logWriter = null;
	/** Config setting. */
	private String maxConnectionsPerPartition="50";
	/** Config setting. */
	private String minConnectionsPerPartition="10";
	/** Config setting. */
	private String preparedStatementCacheSize="50";
	/** Config setting. */
	private String statementsCachedPerConnection="30";
	/** Config setting. */
	private String acquireIncrement="2";
	/** Config setting. */
	private String partitions="2";
	/** Config setting. */
	private String idleConnectionTestPeriod="60";
	/** Config setting. */
	private String idleMaxAge="240";
	/** Config setting. */
	private boolean closeConnectionWatch=false;
	/** Config setting. */
	private String connectionTestStatement;
	/** Config setting. */
	private String jdbcUrl="JDBC URL NOT SET!";
	/** Config setting. */
	private String driverClass=null;
	/** Config setting. */
	private String username="USERNAME NOT SET!";
	/** Config setting. */
	private String password="PASSWORD NOT SET!";
	/** Pool handle. */
	private volatile BoneCP pool = null;
	/** Lock for init. */
	private ReadWriteLock rwl = new ReentrantReadWriteLock();
	/** Config setting. */
	private String releaseHelperThreads = "3";
	/** Config setting. */
	private ConnectionHook connectionHook = null;
	/** Config setting. */
	private String connectionHookClassName;
	/** Class logger. */
	private static final Logger logger = LoggerFactory.getLogger(BoneCPDataSource.class);
	/** For toString(). */
	private static final String CONFIG_TOSTRING = "[BoneCPDataSource] JDBC URL = %s, Username = %s, partitions = %s, max (per partition) = %s, min (per partition) = %s, helper threads = %s, idle max age = %s, idle test period = %s";
	/** config setting. */
	private String initSQL;
	/** If enabled, log SQL statements being executed. */
	private boolean logStatementsEnabled;
	/** If set to true, the connection pool will remain empty until the first connection is obtained. */
	private boolean lazyInit;
	/** After attempting to acquire a connection and failing, wait for this value before attempting to acquire a new connection again. */
	private String acquireRetryDelay;
	/** Classloader to use. */
	private ClassLoader classLoader;
	/** Config object built during init. */
	private BoneCPConfig config;
	/**
	 * Default empty constructor.
	 *
	 */
	public BoneCPDataSource() {
		// default constructor
	}
	/**
	 * 
	 *
	 * @param config
	 */
	public BoneCPDataSource(BoneCPConfig config) {
		this.setJdbcUrl(config.getJdbcUrl());
		this.setUsername(config.getUsername());
		this.setPassword(config.getPassword());
		this.setIdleConnectionTestPeriod(config.getIdleConnectionTestPeriod());
		this.setIdleMaxAge(config.getIdleMaxAge());
		this.setPartitionCount(config.getPartitionCount());
		this.setMaxConnectionsPerPartition(config.getMaxConnectionsPerPartition());
		this.setMinConnectionsPerPartition(config.getMinConnectionsPerPartition());
		this.setAcquireIncrement(config.getAcquireIncrement());
		this.setConnectionTestStatement(config.getConnectionTestStatement());
		this.setPreparedStatementCacheSize(config.getStatementsCacheSize());
		this.setStatementsCachedPerConnection(config.getStatementsCachedPerConnection());
		this.setReleaseHelperThreads(config.getReleaseHelperThreads());
		this.setConnectionHook(config.getConnectionHook());
		this.setInitSQL(config.getInitSQL());
		this.setCloseConnectionWatch(config.isCloseConnectionWatch());
		this.setLogStatementsEnabled(config.isLogStatementsEnabled());
		this.setAcquireRetryDelay(config.getAcquireRetryDelay());
		this.setLazyInit(config.isLazyInit());
	}


	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection()
	throws SQLException {
		if (this.pool == null){
			maybeInit();
		}
		return this.pool.getConnection();
	}

	/**
	 * Close the datasource. 
	 *
	 */
	public void close(){
		this.pool.shutdown();
	}

	/**
	 * @throws SQLException 
	 * 
	 *
	 */
	private void maybeInit() throws SQLException {
		this.rwl.readLock().lock();
		if (this.pool == null){
			this.rwl.readLock().unlock();
			this.rwl.writeLock().lock();
			if (this.pool == null){ //read might have passed, write might not
				int minsize = parseNumber(this.minConnectionsPerPartition, 10);
				int maxsize = parseNumber(this.maxConnectionsPerPartition, 50);
				int releaseHelperThreads = parseNumber(this.releaseHelperThreads, 3);
				int acquireIncrement = parseNumber(this.acquireIncrement, 10);
				int partitions = parseNumber(this.partitions, 3);

				long idleConnectionTestPeriod = parseNumber(this.idleConnectionTestPeriod, 60);
				long idleMaxAge = parseNumber(this.idleMaxAge, 240); 
				int psCacheSize = parseNumber(this.preparedStatementCacheSize, 100);
				int statementsCachedPerConnection = parseNumber(this.statementsCachedPerConnection, 30);
				int acquireRetryDelay = parseNumber(this.acquireRetryDelay, 1000);
				try {
					if (this.driverClass != null){
						loadClass(this.driverClass);
					}
				}
				catch (ClassNotFoundException e) {
					throw new SQLException(e);
				}


				logger.debug(String.format(CONFIG_STATUS, this.jdbcUrl, this.username, minsize, maxsize, acquireIncrement, partitions, idleConnectionTestPeriod/1000, idleMaxAge/(60*1000)));

				this.config = new BoneCPConfig();
				this.config.setMinConnectionsPerPartition(minsize);
				this.config.setMaxConnectionsPerPartition(maxsize);
				this.config.setAcquireIncrement(acquireIncrement);
				this.config.setPartitionCount(partitions);
				this.config.setJdbcUrl(this.jdbcUrl);
				this.config.setUsername(this.username);
				this.config.setPassword(this.password);
				this.config.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
				this.config.setIdleMaxAge(idleMaxAge);
				this.config.setConnectionTestStatement(this.connectionTestStatement);
				this.config.setStatementsCacheSize(psCacheSize);
				this.config.setStatementsCachedPerConnection(statementsCachedPerConnection);
				this.config.setReleaseHelperThreads(releaseHelperThreads);
				this.config.setConnectionHook(this.connectionHook);
				this.config.setInitSQL(this.initSQL);
				this.config.setCloseConnectionWatch(this.closeConnectionWatch);
				this.config.setLogStatementsEnabled(this.logStatementsEnabled);
				this.config.setLazyInit(this.lazyInit);
				this.config.setAcquireRetryDelay(acquireRetryDelay);
				this.pool = new BoneCP(this.config);
			}

			this.rwl.writeLock().unlock(); // Unlock write
		} else {
			this.rwl.readLock().unlock(); // Unlock read
		}
	}


	/** Calls Integer.parseInt but defaults to a given number on parse error.
	 * @param number value to convert
	 * @param defaultValue value to return on no value being set (or error)
	 * @return the converted number (or default) 
	 */
	private int parseNumber(String number, int defaultValue) {
		int result = defaultValue;
		if (number != null) {
			try{
				result = Integer.parseInt(number);
			} catch (NumberFormatException e){
				// do nothing, use the default value
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection(String username, String password)
	throws SQLException {
		throw new UnsupportedOperationException("getConnectionString username, String password) is not supported");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter()
	throws SQLException {
		return this.logWriter;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	public int getLoginTimeout()
	throws SQLException {
		throw new UnsupportedOperationException("getLoginTimeout is unsupported.");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out)
	throws SQLException {
		this.logWriter = out;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds)
	throws SQLException {
		throw new UnsupportedOperationException("setLoginTimeout is unsupported.");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> arg0)
	throws SQLException {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object unwrap(Class arg0)
	throws SQLException {
		return null;
	}




	/**
	 * Gets max connections.
	 *
	 * @return max connections
	 */
	public String getMaxConnectionsPerPartition() {
		return this.maxConnectionsPerPartition;
	}




	/**
	 * Sets max connections. Called via reflection. 
	 *
	 * @param maxConnectionsPerPartition to set 
	 */
	public void setMaxConnectionsPerPartition(String maxConnectionsPerPartition) {
		this.maxConnectionsPerPartition = maxConnectionsPerPartition;
	}

	/**
	 * Sets max connections. Called via reflection. 
	 *
	 * @param maxConnectionsPerPartition to set 
	 */
	public void setMaxConnectionsPerPartition(Integer maxConnectionsPerPartition) {
		this.maxConnectionsPerPartition = maxConnectionsPerPartition.toString();
	}



	/**
	 * Gets minConnectionsPerPartition config setting.
	 *
	 * @return minConnectionsPerPartition
	 */
	public String getMinConnectionsPerPartition() {
		return this.minConnectionsPerPartition;
	}




	/**
	 * Sets minConnectionsPerPartition setting. Called via reflection.
	 *
	 * @param minConnectionsPerPartition 
	 */
	public void setMinConnectionsPerPartition(String minConnectionsPerPartition) {
		this.minConnectionsPerPartition = minConnectionsPerPartition;
	}

	/**
	 * Sets minConnectionsPerPartition setting. Called via reflection.
	 *
	 * @param minConnectionsPerPartition 
	 */
	public void setMinConnectionsPerPartition(Integer minConnectionsPerPartition) {
		this.minConnectionsPerPartition = minConnectionsPerPartition.toString();
	}



	/**
	 * Gets acquireIncrement setting. 
	 *
	 * @return acquireIncrement set in config
	 */
	public String getAcquireIncrement() {
		return this.acquireIncrement;
	}




	/**
	 * Sets acquireIncrement setting. Called via reflection.
	 *
	 * @param acquireIncrement acquire increment setting
	 */
	public void setAcquireIncrement(String acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}


	/**
	 * Sets acquireIncrement setting. Called via reflection.
	 *
	 * @param acquireIncrement acquire increment setting
	 */
	public void setAcquireIncrement(Integer acquireIncrement) {
		this.acquireIncrement = acquireIncrement.toString();
	}



	/**
	 * Gets the number of partitions. 
	 *
	 * @return partitions set in config.
	 */
	public String getPartitions() {
		return this.partitions;
	}




	/**
	 * Sets the number of thread partitions set in config. Called via reflection.
	 *
	 * @param partitions to set
	 */
	public void setPartitionCount(String partitions) {
		this.partitions = partitions;
	}


	/**
	 * Sets the number of thread partitions set in config. Called via reflection.
	 *
	 * @param partitionCount to set
	 */
	public void setPartitionCount(Integer partitionCount) {
		this.partitions = partitionCount.toString();
	}


	/**
	 * Gets idleConnectionTestPeriod config setting. 
	 *
	 * @return idleConnectionTestPeriod set in config
	 */
	public String getIdleConnectionTestPeriod() {
		return this.idleConnectionTestPeriod;
	}




	/**
	 * Sets idle connection test period. Called via reflection.
	 *
	 * @param idleConnectionTestPeriod to set
	 */
	public void setIdleConnectionTestPeriod(String idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
	}


	/**
	 * Sets idle connection test period. Called via reflection.
	 *
	 * @param idleConnectionTestPeriod to set
	 */
	public void setIdleConnectionTestPeriod(Long idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = idleConnectionTestPeriod.toString();
	}



	/**
	 * Gets idle max age. 
	 * 
	 * @return idle max age 
	 */
	public String getIdleMaxAge() {
		return this.idleMaxAge;
	}




	/**
	 * Sets the idle maximum age. Called via reflection.
	 *
	 * @param idleMaxAge to set
	 */
	public void setIdleMaxAge(String idleMaxAge) {
		this.idleMaxAge = idleMaxAge;
	}


	/**
	 * Sets the idle maximum age. 
	 *
	 * @param idleMaxAge to set
	 */
	public void setIdleMaxAge(Long idleMaxAge) {
		this.idleMaxAge = idleMaxAge.toString();
	}


	/**
	 * Gets the JDBC connection url. 
	 *
	 * @return JDBC URL 
	 */
	public String getJdbcUrl() {
		return this.jdbcUrl;
	}




	/**
	 * Sets the JDBC connection url (called via reflection).
	 *
	 * @param jdbcUrl JDBC url connection string. 
	 */
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}




	/**
	 * Gets driver class set in config. 
	 *
	 * @return Driver class set in config
	 */
	public String getDriverClass() {
		return this.driverClass;
	}




	/**
	 * Sets driver to use (called via reflection).
	 *
	 * @param driverClass Driver to use
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}




	/**
	 * Gets username set in the config.
	 *
	 * @return Username set in config
	 */
	public String getUsername() {
		return this.username;
	}




	/**
	 * Sets username (via reflection)
	 *
	 * @param username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}




	/**
	 * Gets password set in config.
	 *
	 * @return Set password
	 */
	public String getPassword() {
		return this.password;
	}




	/**
	 * Sets password. c
	 *
	 * @param password to set 
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * Gets Connection test statement.
	 *
	 * @return connection test statement
	 */
	public String getConnectionTestStatement() {
		return this.connectionTestStatement;
	}


	/**
	 * Sets connection test statement. Called via reflection.
	 *
	 * @param connectionTestStatement to use.
	 */
	public void setConnectionTestStatement(String connectionTestStatement) {
		this.connectionTestStatement = connectionTestStatement;
	}


	/**
	 * Gets preparedStatementCacheSize to set.
	 *
	 * @return preparedStatementCacheSize
	 */
	public String getPreparedStatementCacheSize() {
		return this.preparedStatementCacheSize;
	}


	/**
	 * Sets cache size for prepared statements. Called via reflection 
	 *
	 * @param preparedStatementCacheSize to set
	 */
	public void setPreparedStatementCacheSize(String preparedStatementCacheSize) {
		this.preparedStatementCacheSize = preparedStatementCacheSize;
	}

	/**
	 * Sets cache size for prepared statements. Called via reflection 
	 *
	 * @param preparedStatementCacheSize to set
	 */
	public void setPreparedStatementCacheSize(Integer preparedStatementCacheSize) {
		this.preparedStatementCacheSize = preparedStatementCacheSize.toString();
	}

	/**
	 * Returns the total leased connections.
	 *
	 * @return total leased connections
	 */
	public int getTotalLeased() {
		return this.pool.getTotalLeased();
	}

	/**
	 * Gets no of release helper threads.
	 *
	 * @return release helper threads 
	 */
	public String getReleaseHelperThreads() {
		return this.releaseHelperThreads;
	}

	/**
	 * Sets no of release Helper threads to use. Called via reflection 
	 *
	 * @param releaseHelperThreads to set 
	 */
	public void setReleaseHelperThreads(String releaseHelperThreads) {
		this.releaseHelperThreads = releaseHelperThreads;
	}

	/** Sets no of release Helper threads to use.  
	 *
	 * @param releaseHelperThreads to set 
	 */
	public void setReleaseHelperThreads(Integer releaseHelperThreads) {
		this.releaseHelperThreads = releaseHelperThreads.toString();
	}

	/**
	 * Gets statementsCachedPerConnection.
	 *
	 * @return statementsCachedPerConnection
	 */
	public String getStatementsCachedPerConnection() {
		return this.statementsCachedPerConnection;
	}
	/**
	 * Sets statementsCachedPerConnection. Called via reflection.
	 *
	 * @param statementsCachedPerConnection to set
	 */
	public void setStatementsCachedPerConnection(
			String statementsCachedPerConnection) {
		this.statementsCachedPerConnection = statementsCachedPerConnection;
	}
	/**
	 * Sets statementsCachedPerConnection. Called via reflection.
	 *
	 * @param statementsCachedPerConnection to set
	 */
	public void setStatementsCachedPerConnection(
			Integer statementsCachedPerConnection) {
		this.statementsCachedPerConnection = this.statementsCachedPerConnection.toString();
	}

	/** Returns the connection hook instance.
	 * @return the connectionHook instance.
	 */
	public ConnectionHook getConnectionHook() {
		return this.connectionHook;
	}

	/** Sets the connection hook instance.
	 * @param connectionHook the connectionHook to set
	 */
	public void setConnectionHook(ConnectionHook connectionHook) {
		this.connectionHook = connectionHook;
	}

	/** Sets the connection hook class name.
	 * @param connectionHookClassName the connectionHook class name to set
	 */
	public void setConnectionHookClassName(String connectionHookClassName) {
		this.connectionHookClassName = connectionHookClassName;
		if (connectionHookClassName != null){
			Object hookClass;
			try {
				hookClass = loadClass(connectionHookClassName).newInstance();
				this.connectionHook = (ConnectionHook) hookClass;
			} catch (Exception e) {
				logger.error("Unable to create an instance of the connection hook class ("+connectionHookClassName+")");
				this.connectionHook = null;
			} 
		}
	}
	/** Returns the connection hook class name as passed via the setter
	 * @return the connectionHookClassName.
	 */
	public String getConnectionHookClassName() {
		return this.connectionHookClassName;
	}

	/** Returns the initSQL statement.
	 * @return the initSQL
	 */
	public String getInitSQL() {
		return this.initSQL;
	}
	/** Specifies an initial SQL statement that is run only when a connection is first created. 
	 * @param initSQL the initSQL to set
	 */
	public void setInitSQL(String initSQL) {
		this.initSQL = initSQL;
	}

	/** Returns if BoneCP is configured to create a helper thread to watch over connection acquires that are never released. 
	 * FOR DEBUG PURPOSES ONLY. 
	 * @return the current closeConnectionWatch setting.
	 */
	public boolean isCloseConnectionWatch() {
		return this.closeConnectionWatch;
	}

	/** Instruct the pool to create a helper thread to watch over connection acquires that are never released. This is for debugging
	 * purposes only and will create a new thread for each call to getConnection(). Enabling this option will have a big negative impact 
	 * on pool performance.
	 * @param closeConnectionWatch set to true to enable thread monitoring.
	 */
	public void setCloseConnectionWatch(boolean closeConnectionWatch) {
		this.closeConnectionWatch = closeConnectionWatch;
	}

	/** Returns true if SQL logging is currently enabled, false otherwise.
	 * @return the logStatementsEnabled status
	 */
	public boolean isLogStatementsEnabled() {
		return this.logStatementsEnabled;
	}


	/** If enabled, log SQL statements being executed. 
	 * @param logStatementsEnabled the logStatementsEnabled to set
	 */
	public void setLogStatementsEnabled(boolean logStatementsEnabled) {
		this.logStatementsEnabled = logStatementsEnabled;
	}
	/** Returns the number of ms to wait before attempting to obtain a connection again after a failure.
	 * @return the acquireRetryDelay
	 */
	public String getAcquireRetryDelay() {
		return this.acquireRetryDelay;
	}
	/** Sets the number of ms to wait before attempting to obtain a connection again after a failure.
	 * @param acquireRetryDelay the acquireRetryDelay to set
	 */
	public void setAcquireRetryDelay(String acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}

	/** Sets the number of ms to wait before attempting to obtain a connection again after a failure.
	 * @param acquireRetryDelay the acquireRetryDelay to set
	 */
	public void setAcquireRetryDelay(Integer acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay.toString();
	}

	/** Loads the given class, respecting the given classloader.
	 * @param clazz class to load
	 * @return Loaded class
	 * @throws ClassNotFoundException
	 */
	protected Class<?> loadClass(String clazz) throws ClassNotFoundException {
		if (this.classLoader == null){
			return Class.forName(clazz);
		}

		return Class.forName(clazz, true, this.classLoader);

	}
	/** Returns the currently active classloader. 
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	/** Sets the classloader to use to load JDBC driver and hooks (set to null to use default).
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/** Null-safe toString helper.
	 * @param o to check
	 * @return a printable string.
	 */
	private String safePrint(String o){
		return o != null ? o : "";
	}

	@Override
	public String toString() {

		return String.format(CONFIG_TOSTRING, 
				safePrint(this.jdbcUrl),
				safePrint(this.username), 
				safePrint(this.partitions), 
				safePrint(this.maxConnectionsPerPartition), 
				safePrint(this.minConnectionsPerPartition), 
				safePrint(this.releaseHelperThreads), 
				safePrint(this.idleMaxAge), 
				safePrint(this.idleConnectionTestPeriod));
	}
	
	/** Returns a configuration object built during initialization of the connection pool. 
	 * Only valid when at least one connection has been retrieved.  
	 * @return the config
	 */
	public BoneCPConfig getConfig() {
		return this.config;
	}
	
	/** Returns true if connection pool is to be initialized lazily.
	 * @return lazyInit setting 
	 */
	protected boolean isLazyInit() {
		return this.lazyInit;
	}
	
	/** Set to true to force the connection pool to obtain the initial connections lazily.
	 * @param lazyInit the lazyInit setting to set
	 */
	protected void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}
}
