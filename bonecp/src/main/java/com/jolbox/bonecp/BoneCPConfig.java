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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.jolbox.bonecp.hooks.ConnectionHook;


/**
 * Configuration class.
 *
 * @author wallacew
 */
public class BoneCPConfig implements BoneCPConfigMBean, Cloneable, Serializable {
	/** Serialization UID. */
	private static final long serialVersionUID = 6090570773474131622L;
	/** For toString(). */
	private static final String CONFIG_TOSTRING = "JDBC URL = %s, Username = %s, partitions = %d, max (per partition) = %d, min (per partition) = %d, helper threads = %d, idle max age = %d, idle test period = %d";
	/** Logger class. */
	private static final Logger logger = LoggerFactory.getLogger(BoneCPConfig.class);
	/** Min number of connections per partition. */
	private int minConnectionsPerPartition;
	/** Max number of connections per partition. */
	private int maxConnectionsPerPartition;
	/** Number of new connections to create in 1 batch. */
	private int acquireIncrement = 2;
	/** Number of partitions. */
	private int partitionCount = 2;
	/** DB connection string. */
	private String jdbcUrl;
	/** User name to use. */
	private String username;
	/** Password to use. */
	private String password;
	/** Connections older than this are sent a keep-alive statement. In milliseconds. */
	private long idleConnectionTestPeriod = TimeUnit.MILLISECONDS.convert(240, TimeUnit.MINUTES);
	/** Maximum age of an unused connection before it is closed off. In milliseconds. */ 
	private long idleMaxAge = TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES);
	/** SQL statement to use for keep-alive/test of connection. */
	private String connectionTestStatement;
	/** Min no of prepared statements to cache. */
	private int statementsCacheSize = 100;
	/** No of statements that can be cached per connection. Deprecated. */
	private int statementsCachedPerConnection = 30;
	/** Number of release-connection helper threads to create per partition. */
	private int releaseHelperThreads = 3;
	/** Hook class (external). */
	private ConnectionHook connectionHook;
	/** Query to send once per connection to the database. */
	private String initSQL;
	/** Name of the pool for JMX and thread names. */
	private String poolName;
	/** If set to true, create a new thread that monitors a connection and displays warnings if application failed to 
	 * close the connection. FOR DEBUG PURPOSES ONLY!
	 */
	private boolean closeConnectionWatch;
	/** If set to true, log SQL statements being executed. */ 
	private boolean logStatementsEnabled;
	/** After attempting to acquire a connection and failing, wait for this value before attempting to acquire a new connection again. */
	private int acquireRetryDelay=7000;
	/** After attempting to acquire a connection and failing, try to connect these many times before giving up. */
	private int acquireRetryAttempts=5;
	/** If set to true, the connection pool will remain empty until the first connection is obtained. */
	private boolean lazyInit;
	/** If set to true, stores all activity on this connection to allow for replaying it again. */
	private boolean transactionRecoveryEnabled;
	/** Connection hook class name. */
	private String connectionHookClassName;
	/** Classloader to use when loading the JDBC driver. */
	private ClassLoader classLoader;
	/** Name of the pool for JMX and thread names. */
	private String poolName;
	 /** Set to true to disable JMX. */
	private boolean disableJMX;
	
	/** Returns the name of the pool for JMX and thread names.
	 * @return a pool name.
	 */
	public String getPoolName() {
		return this.poolName;
	}
	
	/** Sets the name of the pool for JMX and thread names.
	 * @param poolName to set.
	 */
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
		
	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getMinConnectionsPerPartition()
	 */
	public int getMinConnectionsPerPartition() {
		return this.minConnectionsPerPartition;
	}

	/**
	 * Sets the minimum number of connections that will be contained in every partition.
	 *
	 * @param minConnectionsPerPartition number of connections
	 */
	public void setMinConnectionsPerPartition(int minConnectionsPerPartition) {
		this.minConnectionsPerPartition = minConnectionsPerPartition;
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getMaxConnectionsPerPartition()
	 */
	public int getMaxConnectionsPerPartition() {
		return this.maxConnectionsPerPartition;
	}

	/**
	 * Sets the maximum number of connections that will be contained in every partition. 
	 * Setting this to 5 with 3 partitions means you will have 15 unique connections to the database. 
	 * Note that the connection pool will not create all these connections in one go but rather start off 
	 * with minConnectionsPerPartition and gradually increase connections as required.
	 *
	 * @param maxConnectionsPerPartition number of connections.
	 */
	public void setMaxConnectionsPerPartition(int maxConnectionsPerPartition) {
		this.maxConnectionsPerPartition = maxConnectionsPerPartition;
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getAcquireIncrement()
	 */
	public int getAcquireIncrement() {
		return this.acquireIncrement;
	}

	/**
	 * Sets the acquireIncrement property. 
	 * 
	 * When the available connections are about to run out, BoneCP will dynamically create new ones in batches. 
	 * This property controls how many new connections to create in one go (up to a maximum of maxConnectionsPerPartition). 
	 * <p>Note: This is a per partition setting.
	 *
	 * @param acquireIncrement value to set. 
	 */
	public void setAcquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getPartitionCount()
	 */
	public int getPartitionCount() {
		return this.partitionCount;
	}

	/**
	 * Sets number of partitions to use. 
	 * 
	 * In order to reduce lock contention and thus improve performance, 
	 * each incoming connection request picks off a connection from a pool that has thread-affinity, 
	 * i.e. pool[threadId % partition_count]. The higher this number, the better your performance will be for the case 
	 * when you have plenty of short-lived threads. Beyond a certain threshold, maintenance of these pools will start 
	 * to have a negative effect on performance (and only for the case when connections on a partition start running out).
	 * 
	 * <p>Default: 2, minimum: 1, recommended: 3-4 (but very app specific)
	 *
	 * @param partitionCount to set 
	 */
	public void setPartitionCount(int partitionCount) {
		this.partitionCount = partitionCount;
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getJdbcUrl()
	 */
	public String getJdbcUrl() {
		return this.jdbcUrl;
	}

	/**
	 * Sets the JDBC connection URL.
	 *
	 * @param jdbcUrl to set
	 */
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	
	public String getPoolName() {
		return this.poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getUsername()
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Sets username to use for connections.
	 *
	 * @param username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets password to use for connections
	 *
	 * @return password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Sets password to use for connections.
	 *
	 * @param password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getIdleConnectionTestPeriod()
	 */
	public long getIdleConnectionTestPeriod() {
		return this.idleConnectionTestPeriod;
	}

	/**
	 * Sets the idleConnectionTestPeriod.
	 * 
	 * This sets the time (in minutes), for a connection to remain idle before sending 
	 * a test query to the DB. This is useful to prevent a DB from timing out connections 
	 * on its end. Do not use aggressive values here!
	 * 
	 * <p>Default: 240 min, set to 0 to disable
	 *
	 * @param idleConnectionTestPeriod to set 
	 */
	public void setIdleConnectionTestPeriod(long idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = TimeUnit.MILLISECONDS.convert(idleConnectionTestPeriod, TimeUnit.MINUTES);
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getIdleMaxAge()
	 */
	public long getIdleMaxAge() {
		return this.idleMaxAge;
	}

	/**
	 * Sets Idle max age (in min).
	 * 
	 * The time (in minutes), for a connection to remain unused before it is closed off. Do not use aggressive values here! 
	 * <p>Default: 60 minutes, set to 0 to disable.
	 *
	 * @param idleMaxAge to set
	 */
	public void setIdleMaxAge(long idleMaxAge) {
		this.idleMaxAge = TimeUnit.MILLISECONDS.convert(idleMaxAge, TimeUnit.MINUTES);
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getConnectionTestStatement()
	 */
	public String getConnectionTestStatement() {
		return this.connectionTestStatement;
	}

	/**
	 *Sets the connection test statement.
	 *
	 *The query to send to the DB to maintain keep-alives and test for dead connections. 
	 *This is database specific and should be set to a query that consumes the minimal amount of load on the server. 
	 *Examples: MySQL: "SELECT 1", PostgreSQL: "SELECT NOW()". 
	 *If you do not set this, then BoneCP will issue a metadata request instead that should work on all databases but is probably slower.
	 *
	 *<p>Default: Use metadata request
	 *
	 * @param connectionTestStatement to set.
	 */
	public void setConnectionTestStatement(String connectionTestStatement) {
		this.connectionTestStatement = connectionTestStatement;
	}

	/** Deprecated. Use getStatementsCacheSize() instead
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getPreparedStatementsCacheSize()
	 */
	@Deprecated
	public int getPreparedStatementsCacheSize() {
		logger.info("Please use getStatementsCacheSize in place of getPreparedStatementsCacheSize. This method has been deprecated.");
		return this.statementsCacheSize;
	}



	/**
	 * Deprecated. Use setStatementsCacheSize() instead. 
	 *
	 * @param preparedStatementsCacheSize to set.
	 */
	@Deprecated
	public void setPreparedStatementsCacheSize(int preparedStatementsCacheSize) {
		logger.info("Please use setStatementsCacheSize in place of setPreparedStatementsCacheSize. This method has been deprecated.");
		this.statementsCacheSize = preparedStatementsCacheSize;
	}

	/**
	 * Sets statementsCacheSize setting.
	 * 
	 * The number of statements to cache. 
	 *
	 * @param statementsCacheSize to set.
	 */
	public void setStatementsCacheSize(int statementsCacheSize) {
		this.statementsCacheSize = statementsCacheSize;
	}
	
	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getStatementsCacheSize()
	 */
	@Override
	public int getStatementsCacheSize() {
		return this.statementsCacheSize;
	}
	
	/**
	 * Deprecated. Use set statementCacheSize instead. 
	 * 
	 * The number of statements to cache. 
	 *
	 * @param statementsCacheSize to set.
	 */
	@Deprecated
	public void setStatementCacheSize(int statementsCacheSize) {
		logger.info("Please use setStatementsCacheSize in place of setStatementCacheSize. This method has been deprecated.");
		this.statementsCacheSize = statementsCacheSize;
	}

	/** Deprecated. Use getStatementsCacheSize instead
	 * @return no of cache size. 
	 */
	@Deprecated
	public int getStatementCacheSize() {
		logger.info("Please use getStatementsCacheSize in place of getStatementCacheSize. This method has been deprecated.");
		return this.statementsCacheSize;
	}


	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getReleaseHelperThreads()
	 */
	public int getReleaseHelperThreads() {
		return this.releaseHelperThreads;
	}


	/**
	 * Sets number of helper threads to create that will handle releasing a connection.
	 *
	 * Useful when your application is doing lots of work on each connection 
	 * (i.e. perform an SQL query, do lots of non-DB stuff and perform another query), 
	 * otherwise will probably slow things down.
	 * 
	 * @param releaseHelperThreads no to release 
	 */
	public void setReleaseHelperThreads(int releaseHelperThreads) {
		this.releaseHelperThreads = releaseHelperThreads;
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getStatementsCachedPerConnection()
	 */
	@Deprecated
	public int getStatementsCachedPerConnection() {
		return this.statementsCachedPerConnection;
	}

	/**
	 * DEPRECATED. No longer necessary to call at all. 
	 * 
	 * Sets no of statements cached per connection. 
	 *
	 * The number of prepared statements to cache per connection. This is usually only useful if you attempt to 
	 * prepare the same prepared statement string in the same connection (usually due to a wrong design condition).
	 *
	 * @param statementsCachedPerConnection to set
	 */
	@Deprecated
	public void setStatementsCachedPerConnection(int statementsCachedPerConnection) {
		this.statementsCachedPerConnection = statementsCachedPerConnection;
	}

	/**
	 * Performs validation on the config object.
	 *
	 */
	public void sanitize(){
		if (this.maxConnectionsPerPartition < 2) {
			logger.warn("Max Connections < 2. Setting to 50");
			this.maxConnectionsPerPartition = 50;
		}
		if (this.minConnectionsPerPartition < 2) {
			logger.warn("Min Connections < 2. Setting to 10");
			this.minConnectionsPerPartition = 10;
		}

		if (this.minConnectionsPerPartition > this.maxConnectionsPerPartition) {
			logger.warn("Min Connections > max connections");
			this.minConnectionsPerPartition = this.maxConnectionsPerPartition;
		}
		if (this.acquireIncrement <= 0) {
			logger.warn("acquireIncrement <= 0. Setting to 1.");
			this.acquireIncrement = 1;
		}
		if (this.partitionCount < 1) {
			logger.warn("partitions < 1! Setting to 3");
			this.partitionCount = 3;
		}

		if (this.releaseHelperThreads < 0){
			logger.warn("releaseHelperThreads < 0! Setting to 3");
			this.releaseHelperThreads = 3;
		}

		if (this.statementsCacheSize < 0) {
			logger.warn("preparedStatementsCacheSize < 0! Setting to 0");
			this.statementsCacheSize = 0;
		}

		if (this.acquireRetryDelay <= 0) {
			this.acquireRetryDelay = 1000;
		}

		if (this.jdbcUrl == null || this.jdbcUrl.trim().equals("")){
			logger.warn("JDBC url was not set in config!");
		}

		if (this.username == null || this.username.trim().equals("")){
			logger.warn("JDBC username was not set in config!");
		}

		if (this.password == null){ 
			logger.warn("JDBC password was not set in config!");
		}


		this.username = this.username == null ? "" : this.username.trim();
		this.jdbcUrl = this.jdbcUrl == null ? "" : this.jdbcUrl.trim();
		this.password = this.password == null ? "" : this.password.trim();
		if (this.connectionTestStatement != null) { 
			this.connectionTestStatement = this.connectionTestStatement.trim();
		}
	}

	@Override
	public String toString() {
		return String.format(CONFIG_TOSTRING, this.jdbcUrl,
				this.username, this.partitionCount, this.maxConnectionsPerPartition, this.minConnectionsPerPartition, 
				this.releaseHelperThreads, this.idleMaxAge, this.idleConnectionTestPeriod);
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getConnectionHook()
	 */
	public ConnectionHook getConnectionHook() {
		return this.connectionHook;
	}

	/** Sets the connection hook.
	 * 
	 * Fully qualified class name that implements the ConnectionHook interface (or extends AbstractConnectionHook). 
	 * BoneCP will callback the specified class according to the connection state (onAcquire, onCheckIn, onCheckout, onDestroy).
	 * 
	 * @param connectionHook the connectionHook to set
	 */
	public void setConnectionHook(ConnectionHook connectionHook) {
		this.connectionHook = connectionHook;
	}

	/** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getInitSQL()
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

	/** Returns if BoneCP is configured to create a helper thread to watch over connection acquires that are never released (or released
	 * twice). 
	 * FOR DEBUG PURPOSES ONLY. 
	 * @return the current closeConnectionWatch setting.
	 */
	public boolean isCloseConnectionWatch() {
		return this.closeConnectionWatch;
	}

	/** Instruct the pool to create a helper thread to watch over connection acquires that are never released (or released twice). 
	 * This is for debugging purposes only and will create a new thread for each call to getConnection(). 
	 * Enabling this option will have a big negative impact on pool performance.
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

	/** Returns the number of ms to wait before attempting to obtain a connection again after a failure. Default: 7000.
	 * @return the acquireRetryDelay
	 */
	public int getAcquireRetryDelay() {
		return this.acquireRetryDelay;
	}

	/** Sets the number of ms to wait before attempting to obtain a connection again after a failure. Default: 7000.
	 * @param acquireRetryDelay the acquireRetryDelay to set
	 */
	public void setAcquireRetryDelay(int acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}

	/** Returns true if connection pool is to be initialized lazily.
	 * @return lazyInit setting 
	 */
	public boolean isLazyInit() {
		return this.lazyInit;
	}

	/** Set to true to force the connection pool to obtain the initial connections lazily.
	 * @param lazyInit the lazyInit setting to set
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}


	@Override
	public BoneCPConfig clone() throws CloneNotSupportedException {
		
		BoneCPConfig clone = (BoneCPConfig)super.clone();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field: fields){
			try {
				field.set(clone, field.get(this));
			} catch (Exception e) {
				// should never happen
			}
		}
		return clone;
	}

	@Override
	public boolean equals(Object obj){
		if (obj == this) { 
			return true; 
		}

		if (obj == null || obj.getClass() != getClass()){ 
			return false; 
		}


		BoneCPConfig that = (BoneCPConfig)obj;
		if ( Objects.equal(this.acquireIncrement, that.getAcquireIncrement())
				&& Objects.equal(this.acquireRetryDelay, that.getAcquireRetryDelay())
				&& Objects.equal(this.closeConnectionWatch, that.isCloseConnectionWatch())
				&& Objects.equal(this.logStatementsEnabled, that.isLogStatementsEnabled())
				&& Objects.equal(this.connectionHook, that.getConnectionHook())
				&& Objects.equal(this.connectionTestStatement, that.getConnectionTestStatement())
				&& Objects.equal(this.idleConnectionTestPeriod, that.getIdleConnectionTestPeriod())
				&& Objects.equal(this.idleMaxAge, that.getIdleMaxAge())
				&& Objects.equal(this.initSQL, that.getInitSQL())
				&& Objects.equal(this.jdbcUrl, that.getJdbcUrl())
				&& Objects.equal(this.maxConnectionsPerPartition, that.getMaxConnectionsPerPartition())
				&& Objects.equal(this.minConnectionsPerPartition, that.getMinConnectionsPerPartition())
				&& Objects.equal(this.partitionCount, that.getPartitionCount())
				&& Objects.equal(this.releaseHelperThreads, that.getReleaseHelperThreads())
				&& Objects.equal(this.statementsCachedPerConnection, that.getStatementsCachedPerConnection())
				&& Objects.equal(this.statementsCacheSize, that.getStatementsCacheSize())
				&& Objects.equal(this.username, that.getUsername())
				&& Objects.equal(this.password, that.getPassword())
				&& Objects.equal(this.lazyInit, that.isLazyInit())
				&& Objects.equal(this.transactionRecoveryEnabled, that.isTransactionRecoveryEnabled())
				&& Objects.equal(this.acquireRetryAttempts, that.getAcquireRetryAttempts())
				
				
		){
			return true;
		} 

		return false;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(this.acquireIncrement, this.acquireRetryDelay, this.closeConnectionWatch, this.logStatementsEnabled, this.connectionHook,
				this.connectionTestStatement, this.idleConnectionTestPeriod, this.idleMaxAge, this.initSQL, this.jdbcUrl, 
				this.maxConnectionsPerPartition, this.minConnectionsPerPartition, this.partitionCount, this.releaseHelperThreads, 
				this.statementsCachedPerConnection, this.statementsCacheSize, this.username, this.password, this.lazyInit, this.transactionRecoveryEnabled,
				this.acquireRetryAttempts);
	}

	/** Returns true if the pool is configured to record all transaction activity and replay the transaction automatically in case
	 * of connection failures.
	 * @return the transactionRecoveryEnabled status
	 */
	public boolean isTransactionRecoveryEnabled() {
		return this.transactionRecoveryEnabled;
	}

	/** Set to true to enable recording of all transaction activity and replay the transaction automatically in case
	 * of a connection failure.
	 * @param transactionRecoveryEnabled the transactionRecoveryEnabled status to set
	 */
	public void setTransactionRecoveryEnabled(boolean transactionRecoveryEnabled) {
		this.transactionRecoveryEnabled = transactionRecoveryEnabled;
	}

	/** After attempting to acquire a connection and failing, try to connect these many times before giving up. Default 5. 
	 * @return the acquireRetryAttempts value
	 */
	public int getAcquireRetryAttempts() {
		return this.acquireRetryAttempts;
	}

	/** After attempting to acquire a connection and failing, try to connect these many times before giving up. Default 5. 
	 * @param acquireRetryAttempts the acquireRetryAttempts to set
	 */
	public void setAcquireRetryAttempts(int acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	/** Sets the connection hook class name. Consider using setConnectionHook() instead.
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
 
	/** Return true if JMX is disabled.
	 * @return the disableJMX.
	 */
	public boolean isDisableJMX() {
		return this.disableJMX;
	}

	/** Set to true to disable JMX.
	 * @param disableJMX the disableJMX to set
	 */
	public void setDisableJMX(boolean disableJMX) {
		this.disableJMX = disableJMX;
	}


	
}
