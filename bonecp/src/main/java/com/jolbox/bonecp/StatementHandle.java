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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import jsr166y.LinkedTransferQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.hooks.ConnectionHook;


/**
 * Wrapper around JDBC Statement.
 *
 * @author wallacew
 * @version $Revision$
 */
public class StatementHandle implements Statement{
	/** Set to true if the connection has been "closed". */
	protected AtomicBoolean logicallyClosed = new AtomicBoolean();
	/** A handle to the actual statement. */
	protected Statement internalStatement;
	/** SQL Statement used for this statement. */
	protected String sql;
	/** Cache pertaining to this statement. */
	protected IStatementCache cache;
	/** Handle to the connection holding this statement. */
	protected ConnectionHandle connectionHandle;
	/** The key to use in the cache. */
	private String cacheKey ;
	/** If enabled, log all statements being executed. */
	protected boolean logStatementsEnabled;
	/** If true, this statement is in the cache. */
	public volatile boolean inCache = false;
	/** Stack trace capture of where this statement was opened. */ 
	public String openStackTrace;
	/** Class logger. */
	protected static Logger logger = LoggerFactory.getLogger(StatementHandle.class);
	/** Config setting converted to nanoseconds. */
	protected long queryExecuteTimeLimit;
	/** Config setting. */
	protected ConnectionHook connectionHook;
	/** If true, we will close off statements in a separate thread. */
	private boolean statementReleaseHelperEnabled;
	/** Scratch queue of statments awaiting to be closed. */
	private LinkedTransferQueue<StatementHandle> statementsPendingRelease;
	/** An opaque object. */
	private Object debugHandle;
	/** if true, we care about statistics. */
	private boolean statisticsEnabled;
	/** Statistics handle. */
	private Statistics statistics;
	/** If true, logging is enabled. */
	protected boolean batchSQLLoggingEnabled;
	/** If true, this statement is being closed off by a separate thread. */
	protected volatile boolean enqueuedForClosure; 
	
	/** For logging purposes - stores parameters to be used for execution. */
	protected final ThreadLocal < Map<Object, Object> > logParams = 
         new ThreadLocal < Map<Object, Object> > () {
             @Override 
             protected Map<Object, Object> initialValue() {
                 return new TreeMap<Object, Object>();
         }
     };
 
     /** Retrieves the thread-local log params map. 
     * @return Map of log params. */ 
     protected Map<Object, Object> getLogParams() {
         return this.logParams.get();
     }
     
     /** for logging of addBatch. */
 	protected final ThreadLocal < StringBuilder > batchSQL = 
          new ThreadLocal < StringBuilder > () {
              @Override 
              protected StringBuilder initialValue() {
                  return new StringBuilder();
          }
      };
  
      /** Retrieves the thread-local batch SQL setting 
      * @return thread-local batch SQL setting. */ 
      protected StringBuilder getBatchSQL() {
          return this.batchSQL.get();
      }
      
      /** Sets the the thread-local batch SQL setting 
       * @param sb batchSQL to set.
       * */ 
      protected void setBatchSQL(StringBuilder sb) {
    	  this.batchSQL.set(sb);
       }
       
     
	/**
	 * Constructor to statement handle wrapper. 
	 *
	 * @param internalStatement handle to actual statement instance.
	 * @param sql statement used for this handle.
	 * @param cache Cache handle 
	 * @param connectionHandle Handle to the connection
	 * @param cacheKey  
	 * @param logStatementsEnabled set to true to log statements. 
	 */
	public StatementHandle(Statement internalStatement, String sql, IStatementCache cache, 
						   ConnectionHandle connectionHandle, String cacheKey, 
						   boolean logStatementsEnabled) {
		this.sql = sql;
		this.internalStatement = internalStatement;
		this.cache = cache;
		this.cacheKey = cacheKey; 
		this.connectionHandle = connectionHandle;
		this.logStatementsEnabled = logStatementsEnabled;
		BoneCPConfig config = connectionHandle.getPool().getConfig();
		this.connectionHook = config.getConnectionHook();
		this.statistics = connectionHandle.getPool().getStatistics();
		this.statisticsEnabled = config.isStatisticsEnabled();

		this.batchSQLLoggingEnabled = this.logStatementsEnabled || this.connectionHook != null;
		this.statementReleaseHelperEnabled = connectionHandle.getPool().isStatementReleaseHelperThreadsConfigured();
		if (this.statementReleaseHelperEnabled){
			this.statementsPendingRelease = connectionHandle.getPool().getStatementsPendingRelease();
		}
		try{
			
			this.queryExecuteTimeLimit = connectionHandle.getOriginatingPartition().getQueryExecuteTimeLimitinNanoSeconds();
		} catch (Exception e){ // safety!
//			this.connectionHook = null;
			this.queryExecuteTimeLimit = 0; 
		}
		// store it in the cache if caching is enabled(unless it's already there). 
		if (this.cache != null){
			this.cache.putIfAbsent(this.cacheKey, this);
		}
	}


	/**
	 * Constructor for empty statement (created via connection.createStatement) 
	 *
	 * @param internalStatement wrapper to statement
	 * @param connectionHandle Handle to the connection that this statement is tied to.
	 * @param logStatementsEnabled set to true to enable sql logging.
	 */
	public StatementHandle(Statement internalStatement, ConnectionHandle connectionHandle, boolean logStatementsEnabled) {
		this(internalStatement, null, null, connectionHandle, null, logStatementsEnabled);
	}



	/** Closes off the statement
	 * @throws SQLException
	 */
	protected void closeStatement() throws SQLException {
		this.logicallyClosed.set(true);
		if (this.logStatementsEnabled || this.connectionHook != null){
			getLogParams().clear();
			setBatchSQL(new StringBuilder());
		}
		if (this.cache == null || !this.inCache){ // no cache = throw it away right now
			this.internalStatement.close();
		}
		this.enqueuedForClosure = false;
	}

	/** Tries to move the item to a waiting consumer. If there's no consumer waiting,
	 * offers the item to the queue if there's space available.  
	 * @param e Item to transfer
	 * @return true if the item was transferred or placed on the queue, false if there are no
	 * waiting clients and there's no more space on the queue.
	 */
	protected boolean tryTransferOffer(StatementHandle e) {
		boolean result = true;
		// if we are using a normal LinkedTransferQueue instead of a bounded one, tryTransfer
		// will never fail.
		assert e.enqueuedForClosure : "Statement is not enqueued";
		if (!this.statementsPendingRelease.tryTransfer(e)){
			result = this.statementsPendingRelease.offer(e);
		}
		return result;
	}
	
	public void close() throws SQLException {
		
		if (this.statementReleaseHelperEnabled){
			this.enqueuedForClosure = true; // stop warning later on.
			// try moving onto queue so that a separate thread will handle it....
			try {
				this.statementsPendingRelease.transfer(this);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		/*	if (!tryTransferOffer(this)){
				this.enqueuedForClosure = false; // we failed to enqueue it.
				// closing off the statement if that fails....
				closeStatement();
			} 
		 */
		} else {
			// otherwise just close it off straight away
			closeStatement();
		}
	}
		

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	// @Override
	public void addBatch(String sql)
	throws SQLException {
		checkClosed();
		try{
			if (this.logStatementsEnabled || this.connectionHook != null){
				getBatchSQL().append(sql);
			}

			this.internalStatement.addBatch(sql);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * Checks if the connection is marked as being logically open and throws an exception if not.
	 * @throws SQLException if connection is marked as logically closed.
	 * 
	 *
	 */
	protected void checkClosed() throws SQLException {
		if (this.logicallyClosed.get()) {
			throw new SQLException("Statement is closed");
		}
	}



	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#cancel()
	 */
	// @Override
	public void cancel()
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.cancel();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#clearBatch()
	 */
	// @Override
	public void clearBatch()
	throws SQLException {
		checkClosed();
		try{
			if (this.logStatementsEnabled || this.connectionHook != null){
				setBatchSQL(new StringBuilder());
			}
			this.internalStatement.clearBatch();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#clearWarnings()
	 */
	// @Override
	public void clearWarnings()
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.clearWarnings();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#execute(java.lang.String)
	 */
	// @Override
	public boolean execute(String sql)
	throws SQLException {
		boolean result = false;
		checkClosed();
		try {
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()));
			}
			long timer = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.execute(sql);
			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			queryTimerEnd(sql, timer);

		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result;
	}




	/** Call the onQueryExecuteTimeLimitExceeded hook if necessary
	 * @param sql sql statement that took too long
	 * @param queryStartTime time when query was started.
	 */
	protected void queryTimerEnd(String sql, long queryStartTime) {
		if ((this.queryExecuteTimeLimit != 0) 
				&& (this.connectionHook != null)){
			long timeElapsed = (System.nanoTime() - queryStartTime);
			
			if (timeElapsed > this.queryExecuteTimeLimit){
				this.connectionHook.onQueryExecuteTimeLimitExceeded(this.connectionHandle, this, sql, getLogParams(), timeElapsed);
			}
		}
		
		if (this.statisticsEnabled){
			this.statistics.incrementStatementsExecuted();
			this.statistics.addStatementExecuteTime(System.nanoTime() - queryStartTime);
			
		}

	}
	

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	// @Override
	public boolean execute(String sql, int autoGeneratedKeys)
	throws SQLException {
		boolean result = false;
		checkClosed();
		try{
			if (this.logStatementsEnabled  && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()));
			}

			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.execute(sql, autoGeneratedKeys);

			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}

			queryTimerEnd(sql, queryStartTime);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result;

	}


	/** Start off a timer if necessary
	 * @return Start time
	 */
	protected long queryTimerStart() {
		return this.statisticsEnabled || ((this.queryExecuteTimeLimit != 0) && (this.connectionHook != null)) ? System.nanoTime() : Long.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	// @Override
	public boolean execute(String sql, int[] columnIndexes)
	throws SQLException {
		boolean result = false;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()));
			}

			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			
			result = this.internalStatement.execute(sql, columnIndexes);
			
			if (this.connectionHook != null){
				// compiler is smart enough to remove this call if it's a no-op as is the default
				// case with the abstract class
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			queryTimerEnd(sql, queryStartTime);

		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	// @Override
	public boolean execute(String sql, String[] columnNames)
	throws SQLException {
		boolean result = false;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()));
			}
			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.execute(sql, columnNames);
			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}

			queryTimerEnd(sql, queryStartTime);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result;

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#executeBatch()
	 */
	// @Override
	public int[] executeBatch()
	throws SQLException {
		int[] result = null;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(this.batchSQL.get().toString(), getLogParams()));
			}
			long queryStartTime = queryTimerStart();
			String query = this.batchSQLLoggingEnabled ? this.batchSQL.get().toString() : "";
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, query, getLogParams());
			}
			result = this.internalStatement.executeBatch();

			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, query, getLogParams());
			}

			queryTimerEnd(this.batchSQLLoggingEnabled ? this.batchSQL.get().toString() : "", queryStartTime);

		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; // never reached

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#executeQuery(java.lang.String)
	 */
	// @Override
	public ResultSet executeQuery(String sql)
	throws SQLException {
		ResultSet result = null;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()));
			}
			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.executeQuery(sql);
			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}

			queryTimerEnd(sql, queryStartTime);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result;

	}


	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String)
	 */
	// @Override
	public int executeUpdate(String sql)
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()));
			}
			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.executeUpdate(sql);
			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}

			queryTimerEnd(sql, queryStartTime);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}


	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	// @Override
	public int executeUpdate(String sql, int autoGeneratedKeys)
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()));
			}
			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.executeUpdate(sql, autoGeneratedKeys);
			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}

			queryTimerEnd(sql, queryStartTime);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}


	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	// @Override
	public int executeUpdate(String sql, int[] columnIndexes)
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()), columnIndexes);
			}
			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.executeUpdate(sql, columnIndexes);
			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}

			queryTimerEnd(sql, queryStartTime);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}


	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	// @Override
	public int executeUpdate(String sql, String[] columnNames)
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			if (this.logStatementsEnabled && logger.isDebugEnabled()){
				logger.debug(PoolUtil.fillLogParams(sql, getLogParams()), columnNames);
			}
			long queryStartTime = queryTimerStart();
			if (this.connectionHook != null){
				this.connectionHook.onBeforeStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}
			result = this.internalStatement.executeUpdate(sql, columnNames);
			if (this.connectionHook != null){
				this.connectionHook.onAfterStatementExecute(this.connectionHandle, this, sql, getLogParams());
			}

			queryTimerEnd(sql, queryStartTime);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);
		}

		return result; 
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getConnection()
	 */
	// @Override
	public Connection getConnection()
	throws SQLException {
		checkClosed();
		return this.connectionHandle;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getFetchDirection()
	 */
	// @Override
	public int getFetchDirection()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getFetchDirection();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getFetchSize()
	 */
	// @Override
	public int getFetchSize()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getFetchSize();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	// @Override
	public ResultSet getGeneratedKeys()
	throws SQLException {
		ResultSet result = null;
		checkClosed();
		try{
			result = this.internalStatement.getGeneratedKeys();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	// @Override
	public int getMaxFieldSize()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getMaxFieldSize();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getMaxRows()
	 */
	// @Override
	public int getMaxRows()
	throws SQLException {
		int result=0;
		checkClosed();
		try{
			result = this.internalStatement.getMaxRows();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}


	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getMoreResults()
	 */
	// @Override
	public boolean getMoreResults()
	throws SQLException {
		boolean result = false;
		checkClosed();
		try{
			result = this.internalStatement.getMoreResults();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	// @Override
	public boolean getMoreResults(int current)
	throws SQLException {
		boolean result = false;
		checkClosed();

		try{ 
			result = this.internalStatement.getMoreResults(current);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	// @Override
	public int getQueryTimeout()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getQueryTimeout();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getResultSet()
	 */
	// @Override
	public ResultSet getResultSet()
	throws SQLException {
		ResultSet result = null;
		checkClosed();
		try{
			result = this.internalStatement.getResultSet();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getResultSetConcurrency()
	 */
	// @Override
	public int getResultSetConcurrency()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getResultSetConcurrency();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	// @Override
	public int getResultSetHoldability()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getResultSetHoldability();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getResultSetType()
	 */
	// @Override
	public int getResultSetType()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getResultSetType();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getUpdateCount()
	 */
	// @Override
	public int getUpdateCount()
	throws SQLException {
		int result = 0;
		checkClosed();
		try{
			result = this.internalStatement.getUpdateCount();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#getWarnings()
	 */
	// @Override
	public SQLWarning getWarnings()
	throws SQLException {
		SQLWarning result = null;
		checkClosed();
		try{
			result = this.internalStatement.getWarnings();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}

	//@Override
	/** Returns true if statement is logically closed
	 * @return True if handle is closed
	 */
	public boolean isClosed() {
		return this.logicallyClosed.get();
	}

	// #ifdef JDK6
	@Override
	public void setPoolable(boolean poolable)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setPoolable(poolable);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}
	
	@Override
	public boolean isWrapperFor(Class<?> iface)
	throws SQLException {
		boolean result = false;
		try{
			result = this.internalStatement.isWrapperFor(iface);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result;
	}

	@Override
	public <T> T unwrap(Class<T> iface)
	throws SQLException {
		T result = null;
		try{
			
			result = this.internalStatement.unwrap(iface);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result;

	}

	@Override
	public boolean isPoolable()
	throws SQLException {
		boolean result = false;
		checkClosed();
		try{
			result = this.internalStatement.isPoolable();
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
		return result; 

	}
	// #endif JDK6
	
	
	public void setCursorName(String name)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setCursorName(name);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#setEscapeProcessing(boolean)
	 */
	// @Override
	public void setEscapeProcessing(boolean enable)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setEscapeProcessing(enable);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	// @Override
	public void setFetchDirection(int direction)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setFetchDirection(direction);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	// @Override
	public void setFetchSize(int rows)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setFetchSize(rows);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	// @Override
	public void setMaxFieldSize(int max)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setMaxFieldSize(max);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	// @Override
	public void setMaxRows(int max)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setMaxRows(max);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}

	}

	
	/**
	 * {@inheritDoc}
	 *
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	// @Override
	public void setQueryTimeout(int seconds)
	throws SQLException {
		checkClosed();
		try{
			this.internalStatement.setQueryTimeout(seconds);
		} catch (SQLException e) {
			throw this.connectionHandle.markPossiblyBroken(e);

		}
	}


	/**
	 * Clears out the cache of statements.
	 */
	protected void clearCache(){
		if (this.cache != null){
			this.cache.clear();
		}
	}



	/**
	 * Marks this statement as being "open"
	 *
	 */
	protected void setLogicallyOpen() {
		this.logicallyClosed.set(false);
	}


	@Override
	public String toString(){
		return this.sql;
	}


	/** Returns the stack trace where this statement was first opened.
	 * @return the openStackTrace
	 */
	public String getOpenStackTrace() {
		return this.openStackTrace;
	}


	/** Sets the stack trace where this statement was first opened.
	 * @param openStackTrace the openStackTrace to set
	 */
	public void setOpenStackTrace(String openStackTrace) {
		this.openStackTrace = openStackTrace;
	}


	/** Returns the statement being wrapped around by this wrapper.
	 * @return the internalStatement being used.
	 */
	public Statement getInternalStatement() {
		return this.internalStatement;
	}


	/** Sets the internal statement used by this wrapper. 
	 * @param internalStatement the internalStatement to set
	 */
	public void setInternalStatement(Statement internalStatement) {
		this.internalStatement = internalStatement;
	}
	
	/** Sets a debugHandle, an object that is not used by the connection pool at all but may be set by an application to track
	 * this particular connection handle for any purpose it deems fit.
	 * @param debugHandle any object.
	 */
	public void setDebugHandle(Object debugHandle) {
		this.debugHandle = debugHandle;
	}


	/**
	 * Returns the debugHandle field.
	 * @return debugHandle
	 */
	public Object getDebugHandle() {
		return this.debugHandle;
	}

	/**
	 * Returns the enqueuedForClosure field.
	 * @return enqueuedForClosure
	 */
	public boolean isEnqueuedForClosure() {
		return this.enqueuedForClosure;
	}
	


}