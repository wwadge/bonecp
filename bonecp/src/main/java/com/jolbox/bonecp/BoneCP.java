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

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;

import jsr166y.LinkedTransferQueue;
import jsr166y.TransferQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.FinalizableReferenceQueue;
import com.jolbox.bonecp.hooks.AcquireFailConfig;



/**
 * Connection pool (main class).
 * @author wwadge
 *
 */
public class BoneCP implements Serializable {
	/** Warning message. */
	private static final String THREAD_CLOSE_CONNECTION_WARNING = "Thread close connection monitoring has been enabled. This will negatively impact on your performance. Only enable this option for debugging purposes!";
	/** Serialization UID */
	private static final long serialVersionUID = -8386816681977604817L;
	/** Exception message. */
	private static final String ERROR_TEST_CONNECTION = "Unable to open a test connection to the given database. JDBC url = %s, username = %s. Terminating connection pool. Original Exception: %s";
	/** Exception message. */
	private static final String SHUTDOWN_LOCATION_TRACE = "Attempting to obtain a connection from a pool that has already been shutdown. \nStack trace of location where pool was shutdown follows:\n";
	/** Exception message. */
	private static final String UNCLOSED_EXCEPTION_MESSAGE = "Connection obtained from thread [%s] was never closed. \nStack trace of location where connection was obtained follows:\n";
	/** JMX constant. */
	public static final String MBEAN_CONFIG = "com.jolbox.bonecp:type=BoneCPConfig";
	/** JMX constant. */
	public static final String MBEAN_BONECP = "com.jolbox.bonecp:type=BoneCP";
	/** Constant for keep-alive test */
	private static final String[] METADATATABLE = new String[] {"TABLE"};
	/** Constant for keep-alive test */
	private static final String KEEPALIVEMETADATA = "BONECPKEEPALIVE";
	/** Create more connections when we hit x% of our possible number of connections. */
	protected final int poolAvailabilityThreshold;
	/** Number of partitions passed in constructor. **/
	private int partitionCount;
	/** Partitions handle. */
	private ConnectionPartition[] partitions;
	/** Handle to factory that creates 1 thread per partition that periodically wakes up and performs some
	 * activity on the connection.
	 */
	private ScheduledExecutorService keepAliveScheduler;
	/** Handle to factory that creates 1 thread per partition that periodically wakes up and performs some
	 * activity on the connection.
	 */
	private ScheduledExecutorService maxAliveScheduler;
	/** Executor for threads watching each partition to dynamically create new threads/kill off excess ones.
	 */
	private ExecutorService connectionsScheduler;
	/** Configuration object used in constructor. */ 
	private BoneCPConfig config;
	/** If set to true, config has specified the use of helper threads. */
	private boolean releaseHelperThreadsConfigured;
	/** pointer to the thread containing the release helper threads. */
	private ExecutorService releaseHelper;
	/** pointer to the service containing the statement close helper threads. */
	private ExecutorService statementCloseHelperExecutor;
	/** Executor service for obtaining a connection in an asynchronous fashion. */
	private ExecutorService asyncExecutor;
	/** Logger class. */
	private static Logger logger = LoggerFactory.getLogger(BoneCP.class);
	/** JMX support. */
	private MBeanServer mbs; 
	/** Prevent repeated termination of all connections when the DB goes down. */
	protected Lock terminationLock = new ReentrantLock();
	/** If set to true, create a new thread that monitors a connection and displays warnings if application failed to 
	 * close the connection.
	 */
	protected boolean closeConnectionWatch = false;
	/** Threads monitoring for bad connection requests. */
	private ExecutorService closeConnectionExecutor;
	/** set to true if the connection pool has been flagged as shutting down. */
	protected volatile boolean poolShuttingDown;
	/** Placeholder to give more useful info in case of a double shutdown. */
	private String shutdownStackTrace;
	/** Reference of objects that are to be watched. */
	private final Map<Connection, Reference<ConnectionHandle>> finalizableRefs = new ConcurrentHashMap<Connection, Reference<ConnectionHandle>>();
	/** Watch for connections that should have been safely closed but the application forgot. */
	private FinalizableReferenceQueue finalizableRefQueue;
	/** Time to wait before timing out the connection. Default in config is Long.MAX_VALUE milliseconds. */
	private long connectionTimeoutInMs;
	/** No of ms to wait for thread.join() in connection watch thread. */
	private long closeConnectionWatchTimeoutInMs;
	/** If set to true, config has specified the use of statement release helper threads. */
	private boolean statementReleaseHelperThreadsConfigured;
	/** Scratch queue of statments awaiting to be closed. */
	private LinkedTransferQueue<StatementHandle> statementsPendingRelease;
	/** if true, we care about statistics. */
	private boolean statisticsEnabled;
	/** statistics handle. */
	private Statistics statistics = new Statistics(this);
	/** Config setting. */
	private Boolean defaultReadOnly;
	/** Config setting. */
	private String defaultCatalog;
	/** Config setting. */
	private int defaultTransactionIsolationValue;
	/** Config setting. */
	private Boolean defaultAutoCommit;
	
	/**
	 * Closes off this connection pool.
	 */
	public synchronized void shutdown(){

		if (!this.poolShuttingDown){
			logger.info("Shutting down connection pool...");
			this.poolShuttingDown = true;

			this.shutdownStackTrace = captureStackTrace(SHUTDOWN_LOCATION_TRACE);
			this.keepAliveScheduler.shutdownNow(); // stop threads from firing.
			this.maxAliveScheduler.shutdownNow(); // stop threads from firing.
			this.connectionsScheduler.shutdownNow(); // stop threads from firing.

			if (this.releaseHelperThreadsConfigured){
				this.releaseHelper.shutdownNow();
			}
			if (this.statementReleaseHelperThreadsConfigured){
				this.statementCloseHelperExecutor.shutdownNow();
			}
			if (this.asyncExecutor != null){
				this.asyncExecutor.shutdownNow();
			}
			if (this.closeConnectionExecutor != null){
				this.closeConnectionExecutor.shutdownNow();
			}
			terminateAllConnections();
			logger.info("Connection pool has been shutdown.");
		}
	}

	/** Just a synonym to shutdown. */
	public void close(){
		shutdown();
	}

	/** Closes off all connections in all partitions. */
	protected void terminateAllConnections(){
		this.terminationLock.lock();
		try{
			// close off all connections.
			for (int i=0; i < this.partitionCount; i++) {
				ConnectionHandle conn;
				while ((conn = this.partitions[i].getFreeConnections().poll()) != null){
					postDestroyConnection(conn);
					conn.setInReplayMode(true); // we're dead, stop attempting to replay anything
					try {
						conn.internalClose();
					} catch (SQLException e) {
						logger.error("Error in attempting to close connection", e);
					}
				}

			}
		} finally {
			this.terminationLock.unlock();
		}
	}

	/** Update counters and call hooks.
	 * @param handle connection handle.
	 */
	protected void postDestroyConnection(ConnectionHandle handle){
		ConnectionPartition partition = handle.getOriginatingPartition();
		partition.updateCreatedConnections(-1);
		partition.setUnableToCreateMoreTransactions(false); // we can create new ones now, this is an optimization

		if (this.finalizableRefQueue != null){
			this.finalizableRefs.remove(handle);
		}
		
		// "Destroying" for us means: don't put it back in the pool.
		if (handle.getConnectionHook() != null){
			handle.getConnectionHook().onDestroy(handle);
		}

	}

	/** Returns a database connection by using Driver.getConnection() or DataSource.getConnection()
	 * @return Connection handle
	 * @throws SQLException on error
	 */
	protected Connection obtainRawInternalConnection()
	throws SQLException {
		Connection result = null;

		DataSource datasourceBean = this.config.getDatasourceBean();
		String url = this.config.getJdbcUrl();
		String username = this.config.getUsername();
		String password = this.config.getPassword();
		Properties props = this.config.getDriverProperties();

		if (datasourceBean != null){
			return (username == null ? datasourceBean.getConnection() : datasourceBean.getConnection(username, password));
		} 

		if (props != null){
			result = DriverManager.getConnection(url, props);
		} else {
			result = DriverManager.getConnection(url, username, password);
		}

		if (this.defaultAutoCommit != null){
			result.setAutoCommit(this.defaultAutoCommit);
		}
		if (this.defaultReadOnly != null){
			result.setReadOnly(this.defaultReadOnly);
		}
		if (this.defaultCatalog != null){
			result.setCatalog(this.defaultCatalog);
		}
		if (this.defaultTransactionIsolationValue != -1){
			result.setTransactionIsolation(this.defaultTransactionIsolationValue);
		}

		return result;
	}

	/**
	 * Constructor.
	 * @param config Configuration for pool
	 * @throws SQLException on error
	 */
	public BoneCP(BoneCPConfig config) throws SQLException {
		this.config = config;
		config.sanitize();

		this.statisticsEnabled = config.isStatisticsEnabled();
		this.closeConnectionWatchTimeoutInMs = config.getCloseConnectionWatchTimeoutInMs();
		this.poolAvailabilityThreshold = config.getPoolAvailabilityThreshold();
		this.connectionTimeoutInMs = config.getConnectionTimeoutInMs();
		if (this.connectionTimeoutInMs == 0){
			this.connectionTimeoutInMs = Long.MAX_VALUE;
		}
		this.defaultReadOnly = config.getDefaultReadOnly();
		this.defaultCatalog = config.getDefaultCatalog();
		this.defaultTransactionIsolationValue = config.getDefaultTransactionIsolationValue();
		this.defaultAutoCommit = config.getDefaultAutoCommit();
		
		AcquireFailConfig acquireConfig = new AcquireFailConfig();
		acquireConfig.setAcquireRetryAttempts(new AtomicInteger(0));
		acquireConfig.setAcquireRetryDelayInMs(0);
		acquireConfig.setLogMessage("Failed to obtain initial connection");
		
		if (!config.isLazyInit()){
			try{
				Connection sanityConnection = obtainRawInternalConnection();
				sanityConnection.close();
			} catch (Exception e){
				if (config.getConnectionHook() != null){
					config.getConnectionHook().onAcquireFail(e, acquireConfig);
				}
// #ifdef JDK6
				throw new SQLException(String.format(ERROR_TEST_CONNECTION, config.getJdbcUrl(), config.getUsername(), PoolUtil.stringifyException(e)), e);
// #endif
/* #ifdef JDK5
				throw new SQLException(String.format(ERROR_TEST_CONNECTION, config.getJdbcUrl(), config.getUsername(), PoolUtil.stringifyException(e)));
#endif JDK5 */

			}
		}
		if (!config.isDisableConnectionTracking()){
			this.finalizableRefQueue = new FinalizableReferenceQueue();
		}
		
		this.asyncExecutor = Executors.newCachedThreadPool();
		int helperThreads = config.getReleaseHelperThreads();
		this.releaseHelperThreadsConfigured = helperThreads > 0;
		
		this.statementReleaseHelperThreadsConfigured = config.getStatementReleaseHelperThreads() > 0;
		this.config = config;
		this.partitions = new ConnectionPartition[config.getPartitionCount()];
		String suffix = "";

		if (config.getPoolName()!=null) {
			suffix="-"+config.getPoolName();
		}
		
		
			
		if (this.releaseHelperThreadsConfigured){
			this.releaseHelper = Executors.newFixedThreadPool(helperThreads*config.getPartitionCount(), new CustomThreadFactory("BoneCP-release-thread-helper-thread"+suffix, true));
		}
		this.keepAliveScheduler =  Executors.newScheduledThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-keep-alive-scheduler"+suffix, true));
		this.maxAliveScheduler =  Executors.newScheduledThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-max-alive-scheduler"+suffix, true));
		this.connectionsScheduler =  Executors.newFixedThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-pool-watch-thread"+suffix, true));

		this.partitionCount = config.getPartitionCount();
		this.closeConnectionWatch = config.isCloseConnectionWatch();
		boolean queueLIFO = config.getServiceOrder() != null && config.getServiceOrder().equalsIgnoreCase("LIFO");
		if (this.closeConnectionWatch){
			logger.warn(THREAD_CLOSE_CONNECTION_WARNING);
			this.closeConnectionExecutor =  Executors.newCachedThreadPool(new CustomThreadFactory("BoneCP-connection-watch-thread"+suffix, true));

		}
		for (int p=0; p < config.getPartitionCount(); p++){

			ConnectionPartition connectionPartition = new ConnectionPartition(this);
			this.partitions[p]=connectionPartition;
			TransferQueue<ConnectionHandle> connectionHandles;
			if (config.getMaxConnectionsPerPartition() == config.getMinConnectionsPerPartition()){
				// if we have a pool that we don't want resized, make it even faster by ignoring
				// the size constraints.
				connectionHandles = queueLIFO ? new LIFOQueue<ConnectionHandle>() :  new LinkedTransferQueue<ConnectionHandle>();
			} else {
				connectionHandles = queueLIFO ? new LIFOQueue<ConnectionHandle>(this.config.getMaxConnectionsPerPartition()) : new BoundedLinkedTransferQueue<ConnectionHandle>(this.config.getMaxConnectionsPerPartition());
			}
			
			this.partitions[p].setFreeConnections(connectionHandles);

			if (!config.isLazyInit()){
				for (int i=0; i < config.getMinConnectionsPerPartition(); i++){
					final ConnectionHandle handle = new ConnectionHandle(config.getJdbcUrl(), config.getUsername(), config.getPassword(), this);
					this.partitions[p].addFreeConnection(handle);
				}

			}

			
			if (config.getIdleConnectionTestPeriodInMinutes() > 0 || config.getIdleMaxAgeInMinutes() > 0){
				
				final Runnable connectionTester = new ConnectionTesterThread(connectionPartition, this.keepAliveScheduler, this, config.getIdleMaxAge(TimeUnit.MILLISECONDS), config.getIdleConnectionTestPeriod(TimeUnit.MILLISECONDS), queueLIFO);
				long delayInMinutes = config.getIdleConnectionTestPeriodInMinutes();
				if (delayInMinutes == 0L){
					delayInMinutes = config.getIdleMaxAgeInMinutes();
				}
				if (config.getIdleMaxAgeInMinutes() != 0 && config.getIdleConnectionTestPeriodInMinutes() != 0 && config.getIdleMaxAgeInMinutes() < delayInMinutes){
					delayInMinutes = config.getIdleMaxAgeInMinutes();
				}
				this.keepAliveScheduler.schedule(connectionTester, delayInMinutes, TimeUnit.MINUTES);
			}

			if (config.getMaxConnectionAgeInSeconds() > 0){
				final Runnable connectionMaxAgeTester = new ConnectionMaxAgeThread(connectionPartition, this.keepAliveScheduler, this, config.getMaxConnectionAge(TimeUnit.MILLISECONDS), queueLIFO);
				this.maxAliveScheduler.schedule(connectionMaxAgeTester, config.getMaxConnectionAgeInSeconds(), TimeUnit.SECONDS);
			}
			// watch this partition for low no of threads
			this.connectionsScheduler.execute(new PoolWatchThread(connectionPartition, this));
		}

		initStmtReleaseHelper(suffix);

		if (!this.config.isDisableJMX()){
			initJMX();
		}
	}

	/** Starts off threads released to statement release helpers.
	 * @param suffix of pool
	 */
	protected void initStmtReleaseHelper(String suffix) {
		// we pick a max size of maxConnections * 3 i.e. 3 statements per connection as our limit
		// anything more than that will mean the statements will start being closed off straight away
		// without enqueing
		this.statementsPendingRelease = new BoundedLinkedTransferQueue<StatementHandle>(this.config.getMaxConnectionsPerPartition()*3);
		int statementReleaseHelperThreads = this.config.getStatementReleaseHelperThreads();

		if (statementReleaseHelperThreads > 0) {
			this.setStatementCloseHelperExecutor(Executors.newFixedThreadPool(statementReleaseHelperThreads, new CustomThreadFactory("BoneCP-statement-close-helper-thread"+suffix, true)));

			for (int i = 0; i < statementReleaseHelperThreads; i++) { 
				// go through pool  rather than statementReleaseHelper directly to aid unit testing (i.e. mocking)
				getStatementCloseHelperExecutor().execute(new StatementReleaseHelperThread(this.statementsPendingRelease, this));
			}
		}
	}

	/**
	 * Initialises JMX stuff.  
	 */
	protected void initJMX() {
		if (this.mbs == null){ // this way makes it easier for mocking.
			this.mbs = ManagementFactory.getPlatformMBeanServer();
		}
		try {
			String suffix = "";

			if (this.config.getPoolName()!=null){
				suffix="-"+this.config.getPoolName();
			}

			ObjectName name = new ObjectName(MBEAN_BONECP +suffix);
			ObjectName configname = new ObjectName(MBEAN_CONFIG + suffix);


			if (!this.mbs.isRegistered(name)){
				this.mbs.registerMBean(this.statistics, name); 
			}
			if (!this.mbs.isRegistered(configname)){
				this.mbs.registerMBean(this.config, configname); 
			}
		} catch (Exception e) {
			logger.error("Unable to start JMX", e);
		}
	}



	/**
	 * Returns a free connection.
	 * @return Connection handle.
	 * @throws SQLException 
	 */
	public Connection getConnection() throws SQLException {
		ConnectionHandle result;
		long statsObtainTime = 0;
		
		if (this.poolShuttingDown){
			throw new SQLException(this.shutdownStackTrace);
		}
	
		int partition = (int) (Thread.currentThread().getId() % this.partitionCount);
		ConnectionPartition connectionPartition = this.partitions[partition];

		if (this.statisticsEnabled){
			statsObtainTime = System.nanoTime();
			this.statistics.incrementConnectionsRequested();
		}
		result = connectionPartition.getFreeConnections().poll();

		if (result == null) { 
			// we ran out of space on this partition, pick another free one
			for (int i=0; i < this.partitionCount; i++){
				if (i == partition) {
					continue; // we already determined it's not here
				}
				result = this.partitions[i].getFreeConnections().poll(); // try our luck with this partition
				connectionPartition = this.partitions[i];
				if (result != null) {
					break;  // we found a connection
				}
			}
		}

		if (!connectionPartition.isUnableToCreateMoreTransactions()){ // unless we can't create any more connections...   
			maybeSignalForMoreConnections(connectionPartition);  // see if we need to create more
		}

		// we still didn't find an empty one, wait forever (or as per config) until our partition is free
		if (result == null) {
			try {
				result = connectionPartition.getFreeConnections().poll(this.connectionTimeoutInMs, TimeUnit.MILLISECONDS);
				if (result == null){
					// 08001 = The application requester is unable to establish the connection.
					throw new SQLException("Timed out waiting for a free available connection.", "08001");
				}
			}
			catch (InterruptedException e) {
				throw new SQLException(e.getMessage());
			}
		}
		result.renewConnection(); // mark it as being logically "open"

		// Give an application a chance to do something with it.
		if (result.getConnectionHook() != null){
			result.getConnectionHook().onCheckOut(result);
		}

		if (this.closeConnectionWatch){ // a debugging tool
			watchConnection(result);
		}

		if (this.statisticsEnabled){
			this.statistics.addCumulativeConnectionWaitTime(System.nanoTime()-statsObtainTime);
		}
		return result;
	}


	/** Starts off a new thread to monitor this connection attempt.
	 * @param connectionHandle to monitor 
	 */
	private void watchConnection(ConnectionHandle connectionHandle) {
		String message = captureStackTrace(UNCLOSED_EXCEPTION_MESSAGE);
		this.closeConnectionExecutor.submit(new CloseThreadMonitor(Thread.currentThread(), connectionHandle, message, this.closeConnectionWatchTimeoutInMs));
	}

	/** Throw an exception to capture it so as to be able to print it out later on
	 * @param message message to display
	 * @return Stack trace message
	 * 
	 */
	protected String captureStackTrace(String message) {
		StringBuilder stringBuilder = new StringBuilder(String.format(message, Thread.currentThread().getName()));		
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(int i = 0; i < trace.length; i++){
			stringBuilder.append(" "+trace[i]+"\r\n");
		}

		stringBuilder.append("");

		return stringBuilder.toString();
	}

	/** Obtain a connection asynchronously by queueing a request to obtain a connection in a separate thread. 
	 * 
	 *  Use as follows:<p>
	 *      Future&lt;Connection&gt; result = pool.getAsyncConnection();<p>
	 *       ... do something else in your application here ...<p>
	 *      Connection connection = result.get(); // get the connection<p>
	 *      
	 * @return A Future task returning a connection. 
	 */
	public Future<Connection> getAsyncConnection(){

		return this.asyncExecutor.submit(new Callable<Connection>() {

			public Connection call() throws Exception {
				return getConnection();
			}});
	}

	/**
	 * Tests if this partition has hit a threshold and signal to the pool watch thread to create new connections
	 * @param connectionPartition to test for.
	 */
	private void maybeSignalForMoreConnections(ConnectionPartition connectionPartition) {

		if (!connectionPartition.isUnableToCreateMoreTransactions() && !this.poolShuttingDown &&  
				connectionPartition.getAvailableConnections()*100/connectionPartition.getMaxConnections() <= this.poolAvailabilityThreshold){
			connectionPartition.getPoolWatchThreadSignalQueue().offer(new Object()); // item being pushed is not important.
		} 
	}

	/**
	 * Releases the given connection back to the pool.
	 *
	 * @param connection to release
	 * @throws SQLException
	 */
	protected void releaseConnection(Connection connection) throws SQLException {
		ConnectionHandle handle = (ConnectionHandle)connection;

		// hook calls
		if (handle.getConnectionHook() != null){
			handle.getConnectionHook().onCheckIn(handle);
		}

		// release immediately or place it in a queue so that another thread will eventually close it. If we're shutting down,
		// close off the connection right away because the helper threads have gone away.
		if (!this.poolShuttingDown && this.releaseHelperThreadsConfigured){
			if (!handle.getOriginatingPartition().getConnectionsPendingRelease().tryTransfer(handle)){
				handle.getOriginatingPartition().getConnectionsPendingRelease().put(handle);
			}
		} else {
			internalReleaseConnection(handle);
		}
	}

	/** Release a connection by placing the connection back in the pool.
	 * @param connectionHandle Connection being released.
	 * @throws SQLException 
	 **/
	protected void internalReleaseConnection(ConnectionHandle connectionHandle) throws SQLException {
		connectionHandle.clearStatementCaches(false);

		if (connectionHandle.getReplayLog() != null){
			connectionHandle.getReplayLog().clear();
			connectionHandle.recoveryResult.getReplaceTarget().clear();
		}

		if (connectionHandle.isExpired() || (!this.poolShuttingDown && connectionHandle.isPossiblyBroken()
				&& !isConnectionHandleAlive(connectionHandle))){

			ConnectionPartition connectionPartition = connectionHandle.getOriginatingPartition();
			maybeSignalForMoreConnections(connectionPartition);

			postDestroyConnection(connectionHandle);
			connectionHandle.clearStatementCaches(true);
			return; // don't place back in queue - connection is broken or expired.
		}


		connectionHandle.setConnectionLastUsedInMs(System.currentTimeMillis());
		if (!this.poolShuttingDown){

			putConnectionBackInPartition(connectionHandle);
		} else {
			connectionHandle.internalClose();
		}
	}

	

	/** Places a connection back in the originating partition.
	 * @param connectionHandle to place back
	 * @throws SQLException on error
	 */
	protected void putConnectionBackInPartition(ConnectionHandle connectionHandle) throws SQLException {
		TransferQueue<ConnectionHandle> queue = connectionHandle.getOriginatingPartition().getFreeConnections();

		if (!queue.tryTransfer(connectionHandle)){
			if (!queue.offer(connectionHandle)){
				connectionHandle.internalClose();
			}
		}


	}


	/** Sends a dummy statement to the server to keep the connection alive
	 * @param connection Connection handle to perform activity on
	 * @return true if test query worked, false otherwise
	 */
	public boolean isConnectionHandleAlive(ConnectionHandle connection) {
		Statement stmt = null;
		boolean result = false; 
		boolean logicallyClosed = connection.logicallyClosed;
		try {
			if (logicallyClosed){
				connection.logicallyClosed = false; // avoid checks later on if it's marked as closed.
			}
			String testStatement = this.config.getConnectionTestStatement();
			ResultSet rs = null;

			if (testStatement == null) {
				// Make a call to fetch the metadata instead of a dummy query.
				rs = connection.getMetaData().getTables( null, null, KEEPALIVEMETADATA, METADATATABLE );
			} else {
				stmt = connection.createStatement();
				stmt.execute(testStatement);
			}


			if (rs != null) { 
				rs.close();
			}

			result = true;
		} catch (SQLException e) {
			// connection must be broken!
			result = false;
		} finally {
			connection.logicallyClosed = logicallyClosed;
			connection.setConnectionLastResetInMs(System.currentTimeMillis());
			result = closeStatement(stmt, result);
		}
		return result;
	}

	/**
	 * @param stmt
	 * @param result
	 * @return false on failure.
	 */
	private boolean closeStatement(Statement stmt, boolean result) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				return false;
			}
		}
		return result;
	}

	/** Return total number of connections currently in use by an application
	 * @return no of leased connections
	 */
	public int getTotalLeased(){
		int total=0;
		for (int i=0; i < this.partitionCount; i++){
			total+=this.partitions[i].getCreatedConnections()-this.partitions[i].getAvailableConnections();
		}
		return total;
	}

	/** Return the number of free connections available to an application right away (excluding connections that can be
	 * created dynamically)
	 * @return number of free connections
	 */
	public int getTotalFree(){
		int total=0;
		for (int i=0; i < this.partitionCount; i++){
			total+=this.partitions[i].getAvailableConnections();
		}
		return total;
	}

	/**
	 * Return total number of connections created in all partitions.
	 *
	 * @return number of created connections
	 */
	public int getTotalCreatedConnections(){
		int total=0;
		for (int i=0; i < this.partitionCount; i++){
			total+=this.partitions[i].getCreatedConnections();
		}
		return total;
	}


	/**
	 * Gets config object.
	 *
	 * @return config object 
	 */
	public BoneCPConfig getConfig() {
		return this.config;
	}

	/**
	 * @return the releaseHelper
	 */
	protected ExecutorService getReleaseHelper() {
		return this.releaseHelper;
	}

	/**
	 * @param releaseHelper the releaseHelper to set
	 */
	protected void setReleaseHelper(ExecutorService releaseHelper) {
		this.releaseHelper = releaseHelper;
	}

	/** Return the finalizable refs handle.
	 * @return the finalizableRefs value.
	 */
	protected Map<Connection, Reference<ConnectionHandle>> getFinalizableRefs() {
		return this.finalizableRefs;
	}

	/** Watch for connections that should have been safely closed but the application forgot.
	 * @return the finalizableRefQueue
	 */
	protected FinalizableReferenceQueue getFinalizableRefQueue() {
		return this.finalizableRefQueue;
	}

	/**
	 * Returns the statementCloseHelper field.
	 * @return statementCloseHelper
	 */
	protected ExecutorService getStatementCloseHelperExecutor() {
		return this.statementCloseHelperExecutor;
	}


	/**
	 * Sets the statementCloseHelper field.
	 * @param statementCloseHelper the statementCloseHelper to set
	 */
	protected void setStatementCloseHelperExecutor(ExecutorService statementCloseHelper) {
		this.statementCloseHelperExecutor = statementCloseHelper;
	}

	/**
	 * Returns the releaseHelperThreadsConfigured field.
	 * @return releaseHelperThreadsConfigured
	 */
	protected boolean isReleaseHelperThreadsConfigured() {
		return this.releaseHelperThreadsConfigured;
	}

	/**
	 * Returns the statementReleaseHelperThreadsConfigured field.
	 * @return statementReleaseHelperThreadsConfigured
	 */
	protected boolean isStatementReleaseHelperThreadsConfigured() {
		return this.statementReleaseHelperThreadsConfigured;
	}

	/**
	 * Returns the statementsPendingRelease field.
	 * @return statementsPendingRelease
	 */
	protected LinkedTransferQueue<StatementHandle> getStatementsPendingRelease() {
		return this.statementsPendingRelease;
	}

	/**
	 * Returns a reference to the statistics class.
	 * @return statistics
	 */
	public Statistics getStatistics() {
		return this.statistics;
	}

}