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
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.FinalizableReferenceQueue;



/**
 * Connection pool (main class).
 * @author wwadge
 *
 */
public class BoneCP implements BoneCPMBean, Serializable {
	/** Serialization UID */
	private static final long serialVersionUID = -8386816681977604817L;
	/** Exception message. */
	private static final String ERROR_TEST_CONNECTION = "Unable to open a test connection to the given database. JDBC url = %s, username = %s. Terminating connection pool.";
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
	/** Create more threads when we hit x% of our possible number of connections. */
	protected static final int HIT_THRESHOLD = 20;
	/** Number of partitions passed in constructor. **/
	private int partitionCount;
	/** Partitions handle. */
	private ConnectionPartition[] partitions;
	/** Handle to factory that creates 1 thread per partition that periodically wakes up and performs some
	 * activity on the connection.
	 */
	private ScheduledExecutorService keepAliveScheduler;
	/** Executor for threads watching each partition to dynamically create new threads/kill off excess ones.
	 */
	private ExecutorService connectionsScheduler;
	/** Configuration object used in constructor. */
	private BoneCPConfig config;
	/** If set to true, config has specified the use of helper threads. */
	private boolean releaseHelperThreadsConfigured;
	/** pointer to the thread containing the release helper threads. */
	private ExecutorService releaseHelper;
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
	protected static final Set<Reference<ConnectionHandle>> finalizableRefs = Collections.synchronizedSet(new HashSet<Reference<ConnectionHandle>>());
	/** Watch for connections that should have been safely closed but the application forgot. */
	protected static final FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();


	/**
	 * Closes off this connection pool.
	 */
	public void shutdown(){
		if (!this.poolShuttingDown){
			logger.info("Shutting down connection pool...");
			this.poolShuttingDown = true;

			this.shutdownStackTrace = captureStackTrace(SHUTDOWN_LOCATION_TRACE);
			this.keepAliveScheduler.shutdownNow(); // stop threads from firing.
			this.connectionsScheduler.shutdownNow(); // stop threads from firing.
			if (this.releaseHelperThreadsConfigured){
				this.releaseHelper.shutdownNow();
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
		if (this.terminationLock.tryLock()){
			try{
				// close off all connections.
				for (int i=0; i < this.partitionCount; i++) {
					ConnectionHandle conn;
					while ((conn = this.partitions[i].getFreeConnections().poll()) != null){
						postDestroyConnection(conn);

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
	}

	/** Update counters and call hooks.
	 * @param handle connection handle.
	 */
	protected void postDestroyConnection(ConnectionHandle handle){
		ConnectionPartition partition = handle.getOriginatingPartition();
		partition.updateCreatedConnections(-1);
		partition.setUnableToCreateMoreTransactions(false); // we can create new ones now

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
		DataSource datasourceBean = this.config.getDatasourceBean();
		String url = this.config.getJdbcUrl();
		String username = this.config.getUsername();
		String password = this.config.getPassword();
		
		if (datasourceBean != null){
			return (username == null ? datasourceBean.getConnection() : datasourceBean.getConnection(username, password));
		} 
			
		return DriverManager.getConnection(url, username, password);
	}

	/**
	 * Constructor.
	 * @param config Configuration for pool
	 * @throws SQLException on error
	 */
	public BoneCP(BoneCPConfig config) throws SQLException {
		this.config = config;
		config.sanitize();

		if (!config.isLazyInit()){
			try{
				Connection sanityConnection = obtainRawInternalConnection();
				sanityConnection.close();
			} catch (Exception e){
				if (config.getConnectionHook() != null){
					config.getConnectionHook().onAcquireFail(e);
				}
				throw new SQLException(String.format(ERROR_TEST_CONNECTION, config.getJdbcUrl(), config.getUsername()), e);
			}
		}
		this.asyncExecutor = Executors.newCachedThreadPool();
		this.releaseHelperThreadsConfigured = config.getReleaseHelperThreads() > 0;
		this.config = config;
		this.partitions = new ConnectionPartition[config.getPartitionCount()];
		String suffix = "";
		
		if (config.getPoolName()!=null) {
			suffix="-"+config.getPoolName();
		}
				
		this.keepAliveScheduler =  Executors.newScheduledThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-keep-alive-scheduler"+suffix, true));
		this.connectionsScheduler =  Executors.newFixedThreadPool(config.getPartitionCount(), new CustomThreadFactory("BoneCP-pool-watch-thread"+suffix, true));
		
		this.partitionCount = config.getPartitionCount();
		this.closeConnectionWatch = config.isCloseConnectionWatch();

		if (this.closeConnectionWatch){
			logger.warn("Thread close connection monitoring has been enabled. This will negatively impact on your performance. Only enable this option for debugging purposes!");
			this.closeConnectionExecutor =  Executors.newCachedThreadPool(new CustomThreadFactory("BoneCP-connection-watch-thread"+suffix, true));

		}
		for (int p=0; p < config.getPartitionCount(); p++){

			ConnectionPartition connectionPartition = new ConnectionPartition(this);
			final Runnable connectionTester = new ConnectionTesterThread(connectionPartition, this.keepAliveScheduler, this);
			this.partitions[p]=connectionPartition;
			this.partitions[p].setFreeConnections(new ArrayBlockingQueue<ConnectionHandle>(config.getMaxConnectionsPerPartition()));

			if (!config.isLazyInit()){
				for (int i=0; i < config.getMinConnectionsPerPartition(); i++){
					final ConnectionHandle handle = new ConnectionHandle(config.getJdbcUrl(), config.getUsername(), config.getPassword(), this);
					this.partitions[p].addFreeConnection(handle);
				}

			}

			if (config.getIdleConnectionTestPeriod() > 0){
				this.keepAliveScheduler.scheduleAtFixedRate(connectionTester, config.getIdleConnectionTestPeriod(), config.getIdleConnectionTestPeriod(), TimeUnit.MILLISECONDS);
			}

			// watch this partition for low no of threads
			this.connectionsScheduler.execute(new PoolWatchThread(connectionPartition, this));
		}

		if (!this.config.isDisableJMX()){
		      initJMX();
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
				this.mbs.registerMBean(this, name); 
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
		if (this.poolShuttingDown){
			throw new SQLException(this.shutdownStackTrace);
		}
		int partition = (int) (Thread.currentThread().getId() % this.partitionCount);
		ConnectionPartition connectionPartition = this.partitions[partition];

		ConnectionHandle result;

		result = connectionPartition.getFreeConnections().poll();

		if (result == null) { 
			// we ran out of space on this partition, pick another free one
			for (int i=0; i < this.partitionCount ; i++){
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

		// we still didn't find an empty one, wait forever until our partition is free
		if (result == null) {
			try {
				result = connectionPartition.getFreeConnections().take();
			}
			catch (InterruptedException e) {
				throw new SQLException(e);
			}
		}
		result.setOriginatingPartition(connectionPartition); // track the winning partition to keep partitions balanced
		result.renewConnection(); // mark it as being logically "open"

		// Give an application a chance to do something with it.
		if (result.getConnectionHook() != null){
			result.getConnectionHook().onCheckOut(result);
		}

		if (this.closeConnectionWatch){ // a debugging tool
			watchConnection(result);
		}


		return result;
	}


	/** Starts off a new thread to monitor this connection attempt.
	 * @param connectionHandle to monitor 
	 */
	private void watchConnection(ConnectionHandle connectionHandle) {
		String message = captureStackTrace(UNCLOSED_EXCEPTION_MESSAGE);
		this.closeConnectionExecutor.submit(new CloseThreadMonitor(Thread.currentThread(), connectionHandle, message));
	}

	/** Throw an exception to capture it so as to be able to print it out later on
	 * @param message message to display
	 * @return Stack trace message
	 * 
	 */
	protected String captureStackTrace(String message) {
		StringBuffer stringBuffer = new StringBuffer(String.format(message, Thread.currentThread().getName()));		
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(int i = 0; i < trace.length; i++){
			stringBuffer.append(" "+trace[i]+"\r\n");
		}

		stringBuffer.append("");

		return stringBuffer.toString();
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

		if (!this.poolShuttingDown && !connectionPartition.isUnableToCreateMoreTransactions() && connectionPartition.getFreeConnections().size()*100/connectionPartition.getMaxConnections() < HIT_THRESHOLD){
			try{
				connectionPartition.lockAlmostFullLock();
				connectionPartition.almostFullSignal();
			} finally {
				connectionPartition.unlockAlmostFullLock(); 
			}
		}


	}

	/**
	 * Releases the given connection back to the pool.
	 *
	 * @param connection to release
	 * @throws SQLException
	 */
	protected void releaseConnection(Connection connection) throws SQLException {

		try {
			ConnectionHandle handle = (ConnectionHandle)connection;

			// hook calls
			if (handle.getConnectionHook() != null){
				handle.getConnectionHook().onCheckIn(handle);
			}

			// release immediately or place it in a queue so that another thread will eventually close it. If we're shutting down,
			// close off the connection right away because the helper threads have gone away.
			if (!this.poolShuttingDown && this.releaseHelperThreadsConfigured){
				handle.getOriginatingPartition().getConnectionsPendingRelease().put(handle);
			} else {
				internalReleaseConnection(handle);
			}
		}
		catch (InterruptedException e) {
			throw new SQLException(e);
		}
	}

	/** Release a connection by placing the connection back in the pool.
	 * @param connectionHandle Connection being released.
	 * @throws InterruptedException 
	 * @throws SQLException 
	 **/
	protected void internalReleaseConnection(ConnectionHandle connectionHandle) throws InterruptedException, SQLException {
		connectionHandle.clearStatementCaches(false);

		if (connectionHandle.getReplayLog() != null){
			connectionHandle.getReplayLog().clear();
			connectionHandle.recoveryResult.getReplaceTarget().clear();
		}

		if (!this.poolShuttingDown && connectionHandle.isPossiblyBroken() && !isConnectionHandleAlive(connectionHandle)){

			ConnectionPartition connectionPartition = connectionHandle.getOriginatingPartition();
			maybeSignalForMoreConnections(connectionPartition);

			postDestroyConnection(connectionHandle);
			return; // don't place back in queue - connection is broken!
		}


		connectionHandle.setConnectionLastUsed(System.currentTimeMillis());
		if (!this.poolShuttingDown){

			putConnectionBackInPartition(connectionHandle);
		} else {
			connectionHandle.internalClose();
		}
	}

	/** Places a connection back in the originating partition.
	 * @param connectionHandle to place back
	 * @throws InterruptedException on interrupt
	 */
	protected void putConnectionBackInPartition(ConnectionHandle connectionHandle) throws InterruptedException {
		connectionHandle.getOriginatingPartition().getFreeConnections().put(connectionHandle);
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
				rs = stmt.executeQuery(testStatement);
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
			total+=this.partitions[i].getCreatedConnections()-this.partitions[i].getFreeConnections().size();
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
			total+=this.partitions[i].getFreeConnections().size();
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

}