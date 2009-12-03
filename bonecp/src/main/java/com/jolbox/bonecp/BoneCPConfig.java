package com.jolbox.bonecp;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 * Helper class to avoid passing in dozens of parameters.
 *
 * @author wallacew
 * @version $Revision$
 */
public class BoneCPConfig {
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
    private static Logger logger = Logger.getLogger(BoneCPConfig.class);
    
    /**
     * Gets minConnectionsPerPartition
     *
     * @return minConnectionsPerPartition
     */
    public int getMinConnectionsPerPartition() {
        return this.minConnectionsPerPartition;
    }
    
    /**
     * Sets minConnectionsPerPartition
     *
     * @param minConnectionsPerPartition 
     */
    public void setMinConnectionsPerPartition(int minConnectionsPerPartition) {
        this.minConnectionsPerPartition = minConnectionsPerPartition;
    }
    
    /**
     * Gets maxConnectionsPerPartition
     *
     * @return maxConnectionsPerPartition
     */
    public int getMaxConnectionsPerPartition() {
        return this.maxConnectionsPerPartition;
    }
    
    /**
     * Sets maxConnectionsPerPartition
     *
     * @param maxConnectionsPerPartition 
     */
    public void setMaxConnectionsPerPartition(int maxConnectionsPerPartition) {
        this.maxConnectionsPerPartition = maxConnectionsPerPartition;
    }
    
    /**
     * Gets acquireIncrement
     *
     * @return acquireIncrement
     */
    public int getAcquireIncrement() {
        return this.acquireIncrement;
    }
    
    /**
     * Sets acquireIncrement
     *
     * @param acquireIncrement 
     */
    public void setAcquireIncrement(int acquireIncrement) {
        this.acquireIncrement = acquireIncrement;
    }
    
    /**
     * Gets partitionCount
     *
     * @return partitionCount
     */
    public int getPartitionCount() {
        return this.partitionCount;
    }
    
    /**
     * Sets partitionCount
     *
     * @param partitionCount 
     */
    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }
    
    /**
     * Gets jdbcUrl
     *
     * @return jdbcUrl
     */
    public String getJdbcUrl() {
        return this.jdbcUrl;
    }
    
    /**
     * Sets jdbcUrl
     *
     * @param jdbcUrl 
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
    
    /**
     * Gets username
     *
     * @return username
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Sets username
     *
     * @param username 
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Gets password
     *
     * @return password
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Sets password
     *
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Gets idleConnectionTestPeriod
     *
     * @return idleConnectionTestPeriod
     */
    public long getIdleConnectionTestPeriod() {
        return this.idleConnectionTestPeriod;
    }
    
    /**
     * Sets idleConnectionTestPeriod
     *
     * @param idleConnectionTestPeriod 
     */
    public void setIdleConnectionTestPeriod(long idleConnectionTestPeriod) {
        this.idleConnectionTestPeriod = idleConnectionTestPeriod;
    }
    
    /**
     * Gets idleMaxAge (time in ms).
     *
     * @return idleMaxAge
     */
    public long getIdleMaxAge() {
        return this.idleMaxAge;
    }
    
    /**
     * Sets Idle max age (in ms).
     *
     * @param idleMaxAge to set
     */
    public void setIdleMaxAge(long idleMaxAge) {
        this.idleMaxAge = idleMaxAge;
    }
    
    /**
     * Gets connectionTestStatement
     *
     * @return connectionTestStatement
     */
    public String getConnectionTestStatement() {
        return this.connectionTestStatement;
    }
    
    /**
     * Sets connectionTestStatement
     *
     * @param connectionTestStatement 
     */
    public void setConnectionTestStatement(String connectionTestStatement) {
        this.connectionTestStatement = connectionTestStatement;
    }
    
    /**
     * Gets preparedStatementsCacheSize
     *
     * @return preparedStatementsCacheSize
     */
    public int getPreparedStatementsCacheSize() {
        return this.preparedStatementsCacheSize;
    }
    
    /**
     * Sets preparedStatementsCacheSize
     *
     * @param preparedStatementsCacheSize 
     */
    public void setPreparedStatementsCacheSize(int preparedStatementsCacheSize) {
        this.preparedStatementsCacheSize = preparedStatementsCacheSize;
    }
    
    /**
     * Gets number of release-connection helper threads to create per partition.
     *
     * @return number of threads 
     */
    public int getReleaseHelperThreads() {
        return this.releaseHelperThreads;
    }

    
    /**
     * Sets number of helper threads.
     *
     * @param releaseHelperThreads no to release 
     */
    public void setReleaseHelperThreads(int releaseHelperThreads) {
        this.releaseHelperThreads = releaseHelperThreads;
    }
    
    /**
	 * Gets no of statements cached per connection.
	 *
	 * @return no of statements cached per connection.
	 */
	public int getStatementsCachedPerConnection() {
		return this.statementsCachedPerConnection;
	}

	/**
	 * Sets no of statements cached per connection. 
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

}
