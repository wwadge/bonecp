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

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	private static final String CONFIG_TOSTRING = "JDBC URL = %s, Username = %s, partitions = %d, max (per partition) = %d, min (per partition) = %d, helper threads = %d, idle max age = %d min, idle test period = %d min";
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
	private long idleConnectionTestPeriod = 240*60*1000; // TimeUnit.MILLISECONDS.convert(240, TimeUnit.MINUTES);
	/** Maximum age of an unused connection before it is closed off. In milliseconds. */ 
	private long idleMaxAge =  60*60*1000; // JDK6: TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES);
	/** SQL statement to use for keep-alive/test of connection. */
	private String connectionTestStatement;
	/** Min no of prepared statements to cache. */
	private int statementsCacheSize = 0;
	/** No of statements that can be cached per connection. Deprecated. */
	private int statementsCachedPerConnection = 0;
	/** Number of release-connection helper threads to create per partition. */
	private int releaseHelperThreads = 3;
	/** Hook class (external). */
	private ConnectionHook connectionHook;
	/** Query to send once per connection to the database. */
	private String initSQL;
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
	private ClassLoader classLoader = this.getClassLoader();
	/** Name of the pool for JMX and thread names. */
	private String poolName;
	/** Set to true to disable JMX. */
	private boolean disableJMX;
	/** If set, use datasourceBean.getConnection() to obtain a new connection. */
	private DataSource datasourceBean;
	/** Queries taking longer than this limit to execute are logged. */ 
	private int queryExecuteTimeLimit = 0;
	/** Create more connections when we hit x% of our possible number of connections. */
	private int poolAvailabilityThreshold = 20;
	/** Disable connection tracking. */
	private boolean disableConnectionTracking;
	/** Used when the alternate way of obtaining a connection is required */
	private Properties driverProperties;
	/** Time to wait before a call to getConnection() times out and returns an error. */ 
	private long connectionTimeout = Long.MAX_VALUE;
	/** Time in ms to wait for close connection watch thread. */
	private long closeConnectionWatchTimeout = 0;
	
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
	 * <p>Note: This value only makes sense when used in conjuction with 
	 * idleMaxAge. 
	 * 
	 * <p>Default: 240 min, set to 0 to disable
	 *
	 * @param idleConnectionTestPeriod to set 
	 */
	public void setIdleConnectionTestPeriod(long idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = 1000*60*idleConnectionTestPeriod; // TimeUnit.MILLISECONDS.convert(idleConnectionTestPeriod, TimeUnit.MINUTES);
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
	 * 
	 * <p>Note: This value only makes sense when used in conjuction with idleConnectionTestPeriod. 
	 * 
	 * <p>Default: 60 minutes, set to 0 to disable.
	 *
	 * @param idleMaxAge to set
	 */
	public void setIdleMaxAge(long idleMaxAge) {
		this.idleMaxAge = 1000*60*idleMaxAge; // TimeUnit.MILLISECONDS.convert(idleMaxAge, TimeUnit.MINUTES);
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
	 *Examples: MySQL: "/* ping *\/ SELECT 1", PostgreSQL: "SELECT NOW()". 
	 *If you do not set this, then BoneCP will issue a metadata request instead that should work on all databases but is probably slower.
	 *
	 * (Note: In MySQL, prefixing the statement by /* ping *\/ makes the driver issue 1 fast packet instead. See 
	 * http://blogs.sun.com/SDNChannel/entry/mysql_tips_for_java_developers )
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
	 * When this value is set to zero, the application thread is blocked until the pool is able to perform all the necessary cleanup to
	 * recycle the connection and make it available for another thread. 
	 * 
	 * When a non-zero value is set, the pool will create threads that will take care of recycling a connection when it is closed (the 
	 * application dumps the connection into a temporary queue to be processed asychronously to the application via the release helper 
	 * threads).
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
		if ((this.poolAvailabilityThreshold < 0) || (this.poolAvailabilityThreshold > 100)){
			this.poolAvailabilityThreshold = 20;
		}
		
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
			logger.warn("partitions < 1! Setting to 1");
			this.partitionCount = 1;
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

		if (this.datasourceBean == null && this.driverProperties == null && 
				(this.username == null || this.username.trim().equals(""))){
			logger.warn("JDBC username was not set in config!");
		}

		if ((this.datasourceBean == null) && (this.driverProperties == null) && (this.password == null)){ 
			logger.warn("JDBC password was not set in config!");
		}
		
		// if no datasource and we have driver properties set...
		if (this.datasourceBean == null && this.driverProperties != null){
			if ((this.driverProperties.get("user") == null) && this.username == null){
				logger.warn("JDBC username not set in driver properties and not set in pool config either");
			} else if ((this.driverProperties.get("user") == null) && this.username != null){
				logger.warn("JDBC username not set in driver properties, copying it from pool config");
				this.driverProperties.setProperty("user", this.username);
			} else if (this.username != null && !this.driverProperties.get("user").equals(this.username)){
				logger.warn("JDBC username set in driver properties does not match the one set in the pool config.  Overriding it with pool config.");
				this.driverProperties.setProperty("user", this.username);
			}  
		}
		
		// if no datasource and we have driver properties set...
		if (this.datasourceBean == null && this.driverProperties != null){
			if ((this.driverProperties.get("password") == null) && this.password == null){
				logger.warn("JDBC password not set in driver properties and not set in pool config either");
			} else if ((this.driverProperties.get("password") == null) && this.password != null){
				logger.warn("JDBC password not set in driver properties, copying it from pool config");
				this.driverProperties.setProperty("password", this.password);
			} else if (this.password != null && !this.driverProperties.get("password").equals(this.password)){
				logger.warn("JDBC password set in driver properties does not match the one set in the pool config. Overriding it with pool config.");
				this.driverProperties.setProperty("password", this.password);
			}
			
			// maintain sanity between the two states 
			this.username = this.driverProperties.getProperty("user");
			this.password = this.driverProperties.getProperty("password");
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
				this.releaseHelperThreads, this.idleMaxAge / (60*1000), /* TimeUnit.MINUTES.convert(this.idleMaxAge, TimeUnit.MILLISECONDS)*/
				this.idleConnectionTestPeriod / (60*1000) /*, TimeUnit.MINUTES.convert(this.idleConnectionTestPeriod, TimeUnit.MILLISECONDS)*/);
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

	/** Returns the bean being used to return a connection.
	 * @return the datasourceBean that was set.
	 */
	public DataSource getDatasourceBean() {
		return this.datasourceBean;
	}

	/** If set, use datasourceBean.getConnection() to obtain a new connection instead of Driver.getConnection().
	 * @param datasourceBean the datasourceBean to set
	 */
	public void setDatasourceBean(DataSource datasourceBean) {
		this.datasourceBean = datasourceBean;
	}


	/**
	 * Default constructor.
	 */
	public BoneCPConfig(){
		// do nothing (default constructor)
	}

	/** Initialize the configuration by loading bonecp-config.xml containing the settings. 
	 * @param sectionName section to load
	 * @throws Exception on parse errors
	 */
	public BoneCPConfig(String sectionName) throws Exception{
		this(BoneCPConfig.class.getResourceAsStream("/bonecp-config.xml"), sectionName);
	}

	/** Initialise the configuration by loading an XML file containing the settings. 
	 * @param xmlConfigFile file to load
	 * @param sectionName section to load
	 * @throws Exception 
	 */
	public BoneCPConfig(InputStream xmlConfigFile, String sectionName) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		// ugly XML parsing, but this is built-in the JDK.
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(xmlConfigFile);
			doc.getDocumentElement().normalize();

			// get the default settings
			Properties settings = parseXML(doc, null);
			if (sectionName != null){
				// override with custom settings
				settings.putAll(parseXML(doc, sectionName));
			}
			// set the properties
			setProperties(settings);

		} catch (Exception e) {
			throw e;
		}
	}

	/** Creates a new config using the given properties.
	 * @param props properties to set.
	 * @throws Exception on error
	 */
	public BoneCPConfig(Properties props) throws Exception {
		this.setProperties(props);
	}

	/** Uppercases the first character.
	 * @param name
	 * @return the same string with the first letter in uppercase
	 */
	private static String upFirst(String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}


	/**
	 * Sets the properties by reading off entries in the given parameter (where each key is equivalent to the field name) 
	 * @param props Parameter list to set
	 * @throws Exception on error
	 */
	public void setProperties(Properties props) throws Exception {
		// Use reflection to read in all possible properties of int, String or boolean.
		for (Field field: BoneCPConfig.class.getDeclaredFields()){
			if (!Modifier.isFinal(field.getModifiers())){ // avoid logger etc.
				if (field.getType().equals(int.class)){
					Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), int.class);
					String val = props.getProperty(field.getName());
					if (val == null){
						val = props.getProperty("bonecp."+field.getName()); // hibernate provider style
					}
					if (val != null) {
						try{
							method.invoke(this, Integer.parseInt(val));
						} catch (NumberFormatException e){
							// do nothing, use the default value
						}
					}
				} if (field.getType().equals(long.class)){
					Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), long.class);
					String val = props.getProperty(field.getName());
					if (val == null){
						val = props.getProperty("bonecp."+field.getName()); // hibernate provider style
					}
					if (val != null) {
						try{
							method.invoke(this, Long.parseLong(val));
						} catch (NumberFormatException e){
							// do nothing, use the default value
						}
					}
				} else if (field.getType().equals(String.class)){
					Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), String.class);
					String val = props.getProperty(field.getName());
					if (val == null){
						val = props.getProperty("bonecp."+field.getName()); // hibernate provider style
					}
					if (val != null) {
						method.invoke(this, val);
					}
				} else if (field.getType().equals(boolean.class)){
					Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), boolean.class);
					String val = props.getProperty(field.getName());
					if (val == null){
						val = props.getProperty("bonecp."+field.getName()); // hibernate provider style
					}
					if (val != null) {
						method.invoke(this, Boolean.parseBoolean(val));
					}
				}
			}
		}
	}
	
	/** Parses the given XML doc to extract the properties and return them into a java.util.Properties.
	 * @param doc to parse
	 * @param sectionName which section to extract
	 * @return Properties map
	 */
	private Properties parseXML(Document doc, String sectionName) {
		Properties results = new Properties();
		NodeList config = null;
		if (sectionName == null){
			config = doc.getElementsByTagName("default-config");
		} else {
			boolean found = false;
			config = doc.getElementsByTagName("named-config");
			if(config != null && config.getLength() > 0) {
				for (int i = 0; i < config.getLength(); i++) {
					Node node = config.item(i);
					if(node.getNodeType() == Node.ELEMENT_NODE ){
						NamedNodeMap attributes = node.getAttributes();
						if (attributes != null && attributes.getLength() > 0){
							Node name = attributes.getNamedItem("name");
							if (name.getNodeValue().equalsIgnoreCase(sectionName)){
								found = true;
								break;
							}
						}
					}
				}
			}
			
			if (!found){
				config = null;
				logger.warn("Did not find "+sectionName+" section in config file. Reverting to defaults.");
			}
		}

		if(config != null && config.getLength() > 0) {
			Node node = config.item(0);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				Element elementEntry = (Element)node;
				NodeList childNodeList = elementEntry.getChildNodes();
				for (int j = 0; j < childNodeList.getLength(); j++) {
					Node node_j = childNodeList.item(j);
					if (node_j.getNodeType() == Node.ELEMENT_NODE) {
						Element piece = (Element) node_j;
						NamedNodeMap attributes = piece.getAttributes();
						if (attributes != null && attributes.getLength() > 0){
							results.put(attributes.item(0).getNodeValue(), piece.getTextContent());
						}
					}
				}
			}
		}

		return results;
	}

	/** Return the query execute time limit.
	 * @return the queryTimeLimit
	 */
	public int getQueryExecuteTimeLimit() {
		return this.queryExecuteTimeLimit;
	}

	/** Queries taking longer than this limit to execute are logged.  
	 * @param queryExecuteTimeLimit the limit to set in milliseconds.
	 */
	public void setQueryExecuteTimeLimit(int queryExecuteTimeLimit) {
		this.queryExecuteTimeLimit = queryExecuteTimeLimit;
	}

	/** Returns the pool watch connection threshold value.
	 * @return the poolAvailabilityThreshold currently set.
	 */
	public int getPoolAvailabilityThreshold() {
		return this.poolAvailabilityThreshold;
	}

	/** Sets the Pool Watch thread threshold.
	 * 
	 * The pool watch thread attempts to maintain a number of connections always available (between minConnections and maxConnections). This
	 * value sets the percentage value to maintain. For example, setting it to 20 means that if the following condition holds:
	 * Free Connections / MaxConnections < poolAvailabilityThreshold
	 * 
	 * new connections will be created. In other words, it tries to keep at least 20% of the pool full of connections. Setting the value
	 * to zero will make the pool create new connections when it needs them but it also means your application may have to wait for new
	 * connections to be obtained at times.
	 * 
	 * Default: 10.
	 *  
	 * @param poolAvailabilityThreshold the poolAvailabilityThreshold to set
	 */
	public void setPoolAvailabilityThreshold(int poolAvailabilityThreshold) {
		this.poolAvailabilityThreshold = poolAvailabilityThreshold;
	}

	/** Returns true if connection tracking has been disabled.
	 * @return the disableConnectionTracking
	 */
	public boolean isDisableConnectionTracking() {
		return this.disableConnectionTracking;
	}

	/** If set to true, the pool will not monitor connections for proper closure. Enable this option if you only ever obtain
	 * your connections via a mechanism that is guaranteed to release the connection back to the pool (eg Spring's jdbcTemplate, 
	 * some kind of transaction manager, etc).
	 * 
	 * @param disableConnectionTracking set to true to disable. Default: false.
	 */
	public void setDisableConnectionTracking(boolean disableConnectionTracking) {
		this.disableConnectionTracking = disableConnectionTracking;
	}

	/** Returns the maximum time (in milliseconds) to wait before a call to getConnection is timed out.
	 * @return the connectionTimeout
	 */
	public long getConnectionTimeout() {
		return this.connectionTimeout;
	}

	/** Sets the maximum time (in milliseconds) to wait before a call to getConnection is timed out. 
	 * 
	 * Default: Long.MAX_VALUE ( = wait forever )
	 * 
	 * @param connectionTimeout the connectionTimeout to set
	 */
	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/** Returns the currently configured driver properties.
	 * @return the driverProperties handle
	 */
	public Properties getDriverProperties() {
		return this.driverProperties;
	}

	/** Sets properties that will be passed on to the driver. 
	 * 
	 * The properties handle should contain a list of arbitrary string tag/value pairs 
	 * as connection arguments; normally at least a "user" and "password" property 
	 * should be included. Failure to include the user or password properties will make the 
	 * pool copy the values given in config.setUsername(..) and config.setPassword(..).  
	 * 
	 * Note that the pool will make a copy of these properties so as not to risk attempting to
	 * create a connection later on with different settings.
	 * 
	 * @param driverProperties the driverProperties to set
	 */
	public void setDriverProperties(Properties driverProperties) {
		if (driverProperties != null){
			// make a copy of the properties so that we don't attempt to create more connections
			// later on and are possibly surprised by having different urls/usernames/etc
			this.driverProperties = new Properties();
			this.driverProperties.putAll(driverProperties);
		}
	}

	/** Returns the no of ms to wait when close connection watch threads are enabled. 0 = wait forever.
	 * @return the watchTimeout currently set.
	 */
	public long getCloseConnectionWatchTimeout() {
		return this.closeConnectionWatchTimeout;
	}

	/** Sets the no of ms to wait when close connection watch threads are enabled. 0 = wait forever.
	 * @param closeConnectionWatchTimeout the watchTimeout to set
	 */
	public void setCloseConnectionWatchTimeout(long closeConnectionWatchTimeout) {
		this.closeConnectionWatchTimeout = closeConnectionWatchTimeout;
	}
}