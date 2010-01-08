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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.hooks.ConnectionHook;


/**
 * Configuration class.
 *
 * @author wallacew
 */
public class BoneCPConfig implements BoneCPConfigMBean {
	/** For toString(). */
    private static final String CONFIG_TOSTRING = "JDBC URL = %s, Username = %s, partitions = %d, max (per partition) = %d, min (per partition) = %d, helper threads = %d, idle max age = %d, idle test period = %d";
	/** Min number of connections per partition. */
    private int minConnectionsPerPartition;
    /** Max number of connections per partition. */
    private int maxConnectionsPerPartition;
    /** Number of new connections to create in 1 batch. */
    private int acquireIncrement;
    /** Number of partitions. */
    private int partitionCount;
    /** DB connection string. */
    private String jdbcUrl;
    /** User name to use. */
    private String username;
    /** Password to use. */
    private String password;
    /** Connections older than this are sent a keep-alive statement. In milliseconds. */
    private long idleConnectionTestPeriod;
    /** Maximum age of an unused connection before it is closed off. In milliseconds*/ 
    private long idleMaxAge;
    /** SQL statement to use for keep-alive/test of connection. */
    private String connectionTestStatement;
    /** Min no of prepared statements to cache. */
    private int preparedStatementsCacheSize;
    /** No of statements that can be cached per connection */
    private int statementsCachedPerConnection;
    	/** Number of release-connection helper threads to create per partition. */
    private int releaseHelperThreads;
    /** Logger class. */
    private static Logger logger = LoggerFactory.getLogger(BoneCPConfig.class);
    /** Hook class (external). */
    private ConnectionHook connectionHook;
    /** Query to send once to the database. */
    private String initSQL;
	/** If set to true, create a new thread that monitors a connection and displays warnings if application failed to 
	 * close the connection. FOR DEBUG PURPOSES ONLY!
	 */
    private boolean closeConnectionWatch;
    /** If set to true, log SQL statements being executed. */ 
    private boolean logStatementsEnabled;
    /** After attempting to acquire a connection and failing, wait for this value before attempting to acquire a new connection again. */
    private int acquireRetryDelay;
    
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
     * <p>Default: 3, minimum: 1, recommended: 3-4 (but very app specific)
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
     * <p>Default: 240 min, set to 0 to disable
     *
     * @param idleConnectionTestPeriod to set 
     */
    public void setIdleConnectionTestPeriod(long idleConnectionTestPeriod) {
        this.idleConnectionTestPeriod = idleConnectionTestPeriod;
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
        this.idleMaxAge = idleMaxAge;
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
    
    /** {@inheritDoc}
	 * @see com.jolbox.bonecp.BoneCPConfigMBean#getPreparedStatementsCacheSize()
	 */
    public int getPreparedStatementsCacheSize() {
        return this.preparedStatementsCacheSize;
    }
    
    /**
     * Sets preparedStatementsCacheSize setting.
     * 
     * The number of prepared statements to cache. BoneCP will actually attempt to cache more 
     * if required but uses SoftReferences for anything beyond the value you set here. 
     * In other words, setting this to 50 will mean that BoneCP will cache at 
     * least 50 prepared statements and any additional statements will also be cached 
     * but will be released by the JVM garbage collector when there is insufficient memory (in a LRU fashion).
     *
     * @param preparedStatementsCacheSize to set.
     */
    public void setPreparedStatementsCacheSize(int preparedStatementsCacheSize) {
        this.preparedStatementsCacheSize = preparedStatementsCacheSize;
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
	public int getStatementsCachedPerConnection() {
		return this.statementsCachedPerConnection;
	}

	/**
	 * Sets no of statements cached per connection. 
	 *
	 * The number of prepared statements to cache per connection. This is usually only useful if you attempt to 
	 * prepare the same prepared statement string in the same connection (usually due to a wrong design condition).
	 *
	 * @param statementsCachedPerConnection to set
	 */
	public void setStatementsCachedPerConnection(int statementsCachedPerConnection) {
		this.statementsCachedPerConnection = statementsCachedPerConnection;
	}

	 /**
     * Performs validation on the config object.
     *
     */
    public void sanitize(){
    	// convert to MS
    	this.idleConnectionTestPeriod =  TimeUnit.MILLISECONDS.convert(this.idleConnectionTestPeriod, TimeUnit.MINUTES);
    	this.idleMaxAge =  TimeUnit.MILLISECONDS.convert(this.idleMaxAge, TimeUnit.MINUTES);

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
        
        if (this.preparedStatementsCacheSize < 0) {
            logger.warn("preparedStatementsCacheSize < 0! Setting to 0");
            this.preparedStatementsCacheSize = 0;
        }
        
        if (this.statementsCachedPerConnection < 1) {
       	   logger.warn("statementsCachedPerConnection < 1! Setting to 30");
        	this.statementsCachedPerConnection = 30;
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

	/** Returns the number of ms to wait before attempting to obtain a connection again after a failure.
	 * @return the acquireRetryDelay
	 */
	public int getAcquireRetryDelay() {
		return this.acquireRetryDelay;
	}

	/** Sets the number of ms to wait before attempting to obtain a connection again after a failure.
	 * @param acquireRetryDelay the acquireRetryDelay to set
	 */
	public void setAcquireRetryDelay(int acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}
}
