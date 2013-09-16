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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jolbox.bonecp.hooks.AcquireFailConfig;
import com.jolbox.bonecp.hooks.ConnectionHook;



/**
 * Connection pool (main class).
 * @author wwadge
 *
 */
public class BoneCP implements Serializable, Closeable {
	/** Warning message. */
	private static final String THREAD_CLOSE_CONNECTION_WARNING = "Thread close connection monitoring has been enabled. This will negatively impact on your performance. Only enable this option for debugging purposes!";
	/** Serialization UID */
	private static final long serialVersionUID = -8386816681977604817L;
	/** Exception message. */
	private static final String ERROR_TEST_CONNECTION = "Unable to open a test connection to the given database. JDBC url = %s, username = %s. Terminating connection pool (set lazyInit to true if you expect to start your database after your app). Original Exception: %s";
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
	protected int partitionCount;
	/** Partitions handle. */
	protected ConnectionPartition[] partitions;
	/** Handle to factory that creates 1 thread per partition that periodically wakes up and performs some
	 * activity on the connection.
	 */
	@VisibleForTesting protected ScheduledExecutorService keepAliveScheduler;
	/** Handle to factory that creates 1 thread per partition that periodically wakes up and performs some
	 * activity on the connection.
	 */
	private ScheduledExecutorService maxAliveScheduler;
	/** Executor for threads watching each partition to dynamically create new threads/kill off excess ones.
	 */
	private ExecutorService connectionsScheduler;
	/** Configuration object used in constructor. */
	@VisibleForTesting protected BoneCPConfig config;
	/** Executor service for obtaining a connection in an asynchronous fashion. */
	private ListeningExecutorService asyncExecutor;
	/** Logger class. */
	private static final Logger logger = LoggerFactory.getLogger(BoneCP.class);
	/** JMX support. */
	private MBeanServer mbs;

	/** If set to true, create a new thread that monitors a connection and displays warnings if application failed to
	 * close the connection.
	 */
	protected boolean closeConnectionWatch = false;
	/** Threads monitoring for bad connection requests. */
	private ExecutorService closeConnectionExecutor;
	/** set to true if the connection pool has been flagged as shutting down. */
	protected volatile boolean poolShuttingDown;
	/** Placeholder to give more useful info in case of a double shutdown. */
	protected String shutdownStackTrace;
	/** Reference of objects that are to be watched. */
	private final Map<Connection, Reference<ConnectionHandle>> finalizableRefs = new ConcurrentHashMap<Connection, Reference<ConnectionHandle>>();
	/** Watch for connections that should have been safely closed but the application forgot. */
	private transient FinalizableReferenceQueue finalizableRefQueue;
	/** Time to wait before timing out the connection. Default in config is Long.MAX_VALUE milliseconds. */
	protected long connectionTimeoutInMs;
	/** No of ms to wait for thread.join() in connection watch thread. */
	private long closeConnectionWatchTimeoutInMs;
	/** if true, we care about statistics. */
	protected boolean statisticsEnabled;
	/** statistics handle. */
	protected Statistics statistics = new Statistics(this);
	/** Config setting. */
	@VisibleForTesting protected boolean nullOnConnectionTimeout;
	/** Config setting. */
	@VisibleForTesting
	protected boolean resetConnectionOnClose;
	/** Config setting. */
	protected boolean cachedPoolStrategy;
	/** Currently active get connection strategy class to use. */
	protected ConnectionStrategy connectionStrategy;
	/** If true, there are no connections to be taken. */
	private AtomicBoolean dbIsDown = new AtomicBoolean();
	/** Config setting. */
	@VisibleForTesting protected Properties clientInfo;
	/** If false, we haven't made a dummy driver call first. */
	@VisibleForTesting protected volatile boolean driverInitialized = false;
	/** Keep track of our jvm version. */
	protected int jvmMajorVersion;
	/** This is moved here to aid testing. */
	protected static String connectionClass = "java.sql.Connection";
 
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
			this.asyncExecutor.shutdownNow();

			try {
				this.connectionsScheduler.awaitTermination(5, TimeUnit.SECONDS);

				this.maxAliveScheduler.awaitTermination(5, TimeUnit.SECONDS);
				this.keepAliveScheduler.awaitTermination(5, TimeUnit.SECONDS);
				this.asyncExecutor.awaitTermination(5, TimeUnit.SECONDS);
				
				if (this.closeConnectionExecutor != null){
					this.closeConnectionExecutor.shutdownNow();
					this.closeConnectionExecutor.awaitTermination(5, TimeUnit.SECONDS);
				}
				
			} catch (InterruptedException e) {
				// do nothing
			}
			this.connectionStrategy.terminateAllConnections();
			unregisterDriver();
			registerUnregisterJMX(false);
			if (finalizableRefQueue != null) {
				finalizableRefQueue.close();
			}
			    logger.info("Connection pool has been shutdown.");
		}
	}

	/** Drops a driver from the DriverManager's list. */
	protected void unregisterDriver(){
		String jdbcURL = this.config.getJdbcUrl();
		if ((jdbcURL != null) && this.config.isDeregisterDriverOnClose()){
			logger.info("Unregistering JDBC driver for : "+jdbcURL);
			try {
				DriverManager.deregisterDriver(DriverManager.getDriver(jdbcURL));
			} catch (SQLException e) {
				logger.info("Unregistering driver failed.", e);
			}
		}
	}

	/** Just a synonym to shutdown. */
	public void close(){
		shutdown();
	}


	/**
	 * Physically close off the internal connection.
	 * @param conn
	 */
	protected void destroyConnection(ConnectionHandle conn) {
		postDestroyConnection(conn);
		conn.setInReplayMode(true); // we're dead, stop attempting to replay anything
		try {
				conn.internalClose();
		} catch (SQLException e) {
			logger.error("Error in attempting to close connection", e);
		}
	}
 
	/** Update counters and call hooks.
	 * @param handle connection handle.
	 */
	protected void postDestroyConnection(ConnectionHandle handle){
		ConnectionPartition partition = handle.getOriginatingPartition();

		if (this.finalizableRefQueue != null && handle.getInternalConnection() != null){ //safety
			this.finalizableRefs.remove(handle.getInternalConnection());
			//			assert o != null : "Did not manage to remove connection from finalizable ref queue";
		}

		partition.updateCreatedConnections(-1);
		partition.setUnableToCreateMoreTransactions(false); // we can create new ones now, this is an optimization


		// "Destroying" for us means: don't put it back in the pool.
		if (handle.getConnectionHook() != null){
			handle.getConnectionHook().onDestroy(handle);
		}

	}
	
	/** Obtains a database connection, retrying if necessary.
	 * @param connectionHandle 
	 * @return A DB connection.
	 * @throws SQLException
	 */
	protected Connection obtainInternalConnection(ConnectionHandle connectionHandle) throws SQLException {
		boolean tryAgain = false;
		Connection result = null;
		Connection oldRawConnection = connectionHandle.getInternalConnection();
		String url = this.getConfig().getJdbcUrl();
		
		int acquireRetryAttempts = this.getConfig().getAcquireRetryAttempts();
		long acquireRetryDelayInMs = this.getConfig().getAcquireRetryDelayInMs();
		AcquireFailConfig acquireConfig = new AcquireFailConfig();
		acquireConfig.setAcquireRetryAttempts(new AtomicInteger(acquireRetryAttempts));
		acquireConfig.setAcquireRetryDelayInMs(acquireRetryDelayInMs);
		acquireConfig.setLogMessage("Failed to acquire connection to "+url);
		ConnectionHook connectionHook = this.getConfig().getConnectionHook();
		do{ 
			result = null;
			try { 
				// keep track of this hook.
				result = this.obtainRawInternalConnection();
				tryAgain = false;

				if (acquireRetryAttempts != this.getConfig().getAcquireRetryAttempts()){
					logger.info("Successfully re-established connection to "+url);
				}
				
				this.getDbIsDown().set(false);
				
				connectionHandle.setInternalConnection(result);
				
				// call the hook, if available.
				if (connectionHook != null){
					connectionHook.onAcquire(connectionHandle);
				}

				
				ConnectionHandle.sendInitSQL(result, this.getConfig().getInitSQL());
			} catch (SQLException e) {
				// call the hook, if available.
				if (connectionHook != null){
					tryAgain = connectionHook.onAcquireFail(e, acquireConfig);
				} else {
					logger.error(String.format("Failed to acquire connection to %s. Sleeping for %d ms. Attempts left: %d", url, acquireRetryDelayInMs, acquireRetryAttempts), e);

					try {
						if (acquireRetryAttempts > 0){
							Thread.sleep(acquireRetryDelayInMs);
	 					}
						tryAgain = (acquireRetryAttempts--) > 0;
					} catch (InterruptedException e1) {
						tryAgain=false;
					}
				}
				if (!tryAgain){
					if (oldRawConnection != null) {
						oldRawConnection.close();
					}
					if (result != null) {
						result.close();
					}
					connectionHandle.setInternalConnection(oldRawConnection);
					throw e;
				}
			}
		} while (tryAgain);

		return result;

	}

	/** Returns a database connection by using Driver.getConnection() or DataSource.getConnection()
	 * @return Connection handle
	 * @throws SQLException on error
	 */
	@SuppressWarnings("resource")
	protected Connection obtainRawInternalConnection()
	throws SQLException {
		Connection result = null;

		DataSource datasourceBean = this.config.getDatasourceBean();
		String url = this.config.getJdbcUrl();
		String username = this.config.getUsername();
		String password = this.config.getPassword();
		Properties props = this.config.getDriverProperties();
		boolean externalAuth = this.config.isExternalAuth();
		if (externalAuth && 
				props == null){
			props = new Properties();
		}

		if (datasourceBean != null){
			return (username == null ? datasourceBean.getConnection() : datasourceBean.getConnection(username, password));
		}

		// just force the driver to init first
		if (!this.driverInitialized ){
			try{
				this.driverInitialized = true;
				if (props != null){
					result = DriverManager.getConnection(url, props);
				} else {
					result = DriverManager.getConnection(url, username, password);
				}
				result.close();
			}catch (SQLException t){
				// just force the driver to init first
				// See https://bugs.launchpad.net/bonecp/+bug/876476
			}
		}

		if (props != null){
			result = DriverManager.getConnection(url, props);
		} else { 
			result = DriverManager.getConnection(url, username, password);
		}
		// #ifdef JDK>6
		if (this.clientInfo != null){ // we take care of null'ing this in the constructor if jdk < 6
			result.setClientInfo(this.clientInfo);
		}
		// #endif JDK>6

		return result;
	}

	/**
	 * Constructor.
	 * @param config Configuration for pool
	 * @throws SQLException on error
	 */
	public BoneCP(BoneCPConfig config) throws SQLException {
		Class<?> clazz;
		try {
			jvmMajorVersion = 5;
			clazz = Class.forName(connectionClass , true, config.getClassLoader());
			clazz.getMethod("createClob"); // since 1.6
			jvmMajorVersion = 6;
			clazz.getMethod("getNetworkTimeout"); // since 1.7
			jvmMajorVersion = 7;
		} catch (Exception e) {
			// do nothing
		}
		try {
			this.config = Preconditions.checkNotNull(config).clone(); // immutable
		} catch (CloneNotSupportedException e1) {
			throw new SQLException("Cloning of the config failed");
		}
		this.config.sanitize();

		this.statisticsEnabled = config.isStatisticsEnabled();
		this.closeConnectionWatchTimeoutInMs = config.getCloseConnectionWatchTimeoutInMs();
		this.poolAvailabilityThreshold = config.getPoolAvailabilityThreshold();
		this.connectionTimeoutInMs = config.getConnectionTimeoutInMs();

		if (this.connectionTimeoutInMs == 0){
			this.connectionTimeoutInMs = Long.MAX_VALUE;
		}
		this.nullOnConnectionTimeout = config.isNullOnConnectionTimeout();
		this.resetConnectionOnClose = config.isResetConnectionOnClose();
		this.clientInfo = jvmMajorVersion > 5  ? config.getClientInfo() : null;
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
				throw PoolUtil.generateSQLException(String.format(ERROR_TEST_CONNECTION, config.getJdbcUrl(), config.getUsername(), PoolUtil.stringifyException(e)), e);

			}
		}
		if (!config.isDisableConnectionTracking()){
			this.finalizableRefQueue = new FinalizableReferenceQueue();
		}

		this.asyncExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

		this.config = config;
		this.partitions = new ConnectionPartition[config.getPartitionCount()];
		String suffix = "";

		if (config.getPoolName()!=null) {
			suffix="-"+config.getPoolName();
		}


		this.keepAliveScheduler =  Executors.newScheduledThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-keep-alive-scheduler"+suffix, true));
		this.maxAliveScheduler =  Executors.newScheduledThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-max-alive-scheduler"+suffix, true));
		this.connectionsScheduler =  Executors.newFixedThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-pool-watch-thread"+suffix, true));

		this.partitionCount = config.getPartitionCount();
		this.closeConnectionWatch = config.isCloseConnectionWatch();
		this.cachedPoolStrategy = config.getPoolStrategy() != null && config.getPoolStrategy().equalsIgnoreCase("CACHED");
		if (this.cachedPoolStrategy){
			this.connectionStrategy = new CachedConnectionStrategy(this, new DefaultConnectionStrategy(this));
		} else {
			this.connectionStrategy = new DefaultConnectionStrategy(this);
		}
		boolean queueLIFO = config.getServiceOrder() != null && config.getServiceOrder().equalsIgnoreCase("LIFO");
		if (this.closeConnectionWatch){
			logger.warn(THREAD_CLOSE_CONNECTION_WARNING);
			this.closeConnectionExecutor =  Executors.newCachedThreadPool(new CustomThreadFactory("BoneCP-connection-watch-thread"+suffix, true));

		}
		for (int p=0; p < config.getPartitionCount(); p++){

			ConnectionPartition connectionPartition = new ConnectionPartition(this);
			this.partitions[p]=connectionPartition;
			BlockingQueue<ConnectionHandle> connectionHandles = new LinkedBlockingQueue<ConnectionHandle>(this.config.getMaxConnectionsPerPartition());

			this.partitions[p].setFreeConnections(connectionHandles);

			if (!config.isLazyInit()){
				for (int i=0; i < config.getMinConnectionsPerPartition(); i++){
					this.partitions[p].addFreeConnection(new ConnectionHandle(null, this.partitions[p], this, false));
				}

			}


			if (config.getIdleConnectionTestPeriod(TimeUnit.SECONDS) > 0 || config.getIdleMaxAge(TimeUnit.SECONDS) > 0){

				final Runnable connectionTester = new ConnectionTesterThread(connectionPartition, this.keepAliveScheduler, this, config.getIdleMaxAge(TimeUnit.MILLISECONDS), config.getIdleConnectionTestPeriod(TimeUnit.MILLISECONDS), queueLIFO);
				long delayInSeconds = config.getIdleConnectionTestPeriod(TimeUnit.SECONDS);
				if (delayInSeconds == 0L){
					delayInSeconds = config.getIdleMaxAge(TimeUnit.SECONDS);
				}
				if (config.getIdleMaxAge(TimeUnit.SECONDS) < delayInSeconds
						&& config.getIdleConnectionTestPeriod(TimeUnit.SECONDS) != 0 
						&& config.getIdleMaxAge(TimeUnit.SECONDS) != 0){
					delayInSeconds = config.getIdleMaxAge(TimeUnit.SECONDS);
				}
				this.keepAliveScheduler.schedule(connectionTester, delayInSeconds, TimeUnit.SECONDS);
			}


			if (config.getMaxConnectionAgeInSeconds() > 0){
				final Runnable connectionMaxAgeTester = new ConnectionMaxAgeThread(connectionPartition, this.maxAliveScheduler, this, config.getMaxConnectionAge(TimeUnit.MILLISECONDS), queueLIFO);
				this.maxAliveScheduler.schedule(connectionMaxAgeTester, config.getMaxConnectionAgeInSeconds(), TimeUnit.SECONDS);
			}
			// watch this partition for low no of threads
			this.connectionsScheduler.execute(new PoolWatchThread(connectionPartition, this));
		}

		if (!this.config.isDisableJMX()){
			registerUnregisterJMX(true);
		}


	}


	/**
	 * Initialises JMX stuff.
	 * @param doRegister if true, perform registration, if false unregister
	 */
	protected void registerUnregisterJMX(boolean doRegister) {
		if (this.mbs == null ){ // this way makes it easier for mocking.
			this.mbs = ManagementFactory.getPlatformMBeanServer();
		}
		try {
			String suffix = "";

			if (this.config.getPoolName()!=null){
				suffix="-"+this.config.getPoolName();
			}

			ObjectName name = new ObjectName(MBEAN_BONECP +suffix);
			ObjectName configname = new ObjectName(MBEAN_CONFIG + suffix);


			if (doRegister){
				if (!this.mbs.isRegistered(name)){
					this.mbs.registerMBean(this.statistics, name);
				}
				if (!this.mbs.isRegistered(configname)){
					this.mbs.registerMBean(this.config, configname);
				}
			} else {
				if (this.mbs.isRegistered(name)){
					this.mbs.unregisterMBean(name);
				}
				if (this.mbs.isRegistered(configname)){
					this.mbs.unregisterMBean(configname);
				}
			}
		} catch (Exception e) {
			logger.error("Unable to start/stop JMX", e);
		}
	}


	/**
	 * Returns a free connection.
	 * @return Connection handle.
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return this.connectionStrategy.getConnection();
	}


	/** Starts off a new thread to monitor this connection attempt.
	 * @param connectionHandle to monitor
	 */
	protected void watchConnection(ConnectionHandle connectionHandle) {
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
	public ListenableFuture<Connection> getAsyncConnection(){

		return this.asyncExecutor.submit(new Callable<Connection>() {

			public Connection call() throws Exception {
				return getConnection();
			}});
	}

	/**
	 * Tests if this partition has hit a threshold and signal to the pool watch thread to create new connections
	 * @param connectionPartition to test for.
	 */
	protected void maybeSignalForMoreConnections(ConnectionPartition connectionPartition) {

		if (!connectionPartition.isUnableToCreateMoreTransactions() 
				&& !this.poolShuttingDown &&
				connectionPartition.getAvailableConnections()*100/connectionPartition.getMaxConnections() <= this.poolAvailabilityThreshold){
			connectionPartition.getPoolWatchThreadSignalQueue().offer(new Object()); // item being pushed is not important.
		}
	}

	/**
	 * Releases the given connection back to the pool. This method is not intended to be called by
	 * applications (hence set to protected). Call connection.close() instead which will return
	 * the connection back to the pool.
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
		if (!this.poolShuttingDown){
			internalReleaseConnection(handle);
		}
	}

	/** Release a connection by placing the connection back in the pool.
	 * @param connectionHandle Connection being released.
	 * @throws SQLException
	 **/
	protected void internalReleaseConnection(ConnectionHandle connectionHandle) throws SQLException {
		if (!this.cachedPoolStrategy){
			connectionHandle.clearStatementCaches(false);
		}

		if (connectionHandle.getReplayLog() != null){
			connectionHandle.getReplayLog().clear();
			connectionHandle.recoveryResult.getReplaceTarget().clear();
		}

		if (connectionHandle.isExpired() || 
				(!this.poolShuttingDown 
						&& connectionHandle.isPossiblyBroken()
				&& !isConnectionHandleAlive(connectionHandle))){

            if (connectionHandle.isExpired()) {
                connectionHandle.internalClose();
            }

			ConnectionPartition connectionPartition = connectionHandle.getOriginatingPartition();
			postDestroyConnection(connectionHandle);

			maybeSignalForMoreConnections(connectionPartition);
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

		if (this.cachedPoolStrategy && ((CachedConnectionStrategy)this.connectionStrategy).tlConnections.dumbGet().getValue()){
			connectionHandle.logicallyClosed.set(true);
			((CachedConnectionStrategy)this.connectionStrategy).tlConnections.set(new AbstractMap.SimpleEntry<ConnectionHandle, Boolean>(connectionHandle, false));
		} else {
			BlockingQueue<ConnectionHandle> queue = connectionHandle.getOriginatingPartition().getFreeConnections();
				if (!queue.offer(connectionHandle)){ // this shouldn't fail
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
		boolean logicallyClosed = connection.logicallyClosed.get();
		try {
			connection.logicallyClosed.compareAndSet(true, false); // avoid checks later on if it's marked as closed.
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
			connection.logicallyClosed.set(logicallyClosed);
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
		for (int i=0; i < this.partitionCount && this.partitions[i] != null; i++){
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
		for (int i=0; i < this.partitionCount && this.partitions[i] != null ; i++){
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
		for (int i=0; i < this.partitionCount && this.partitions[i] != null; i++){
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
	 * Returns a reference to the statistics class.
	 * @return statistics
	 */
	public Statistics getStatistics() {
		return this.statistics;
	}

	/**
	 * Returns the dbIsDown field.
	 * @return dbIsDown
	 */
	public AtomicBoolean getDbIsDown() {
		return this.dbIsDown;
	}


}
