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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.jolbox.bonecp.hooks.ConnectionHook;

/**
 * Connection handle wrapper around a JDBC connection.
 * 
 * @author wwadge
 * 
 */
public class ConnectionHandle implements Connection {
	/** Exception message. */
	private static final String LOG_ERROR_MESSAGE = "Connection closed twice exception detected.\n%s\n%s\n";
	/** Exception message. */
	private static final String CLOSED_TWICE_EXCEPTION_MESSAGE = "Connection closed from thread [%s] was closed again.\nStack trace of location where connection was first closed follows:\n";
	/** Connection handle. */
	private Connection connection = null;
	/** Last time this connection was used by an application. */
	private long connectionLastUsed = System.currentTimeMillis();
	/** Last time we sent a reset to this connection. */
	private long connectionLastReset = System.currentTimeMillis();
	/** Pool handle. */
	private BoneCP pool;
	/**
	 * If true, this connection might have failed communicating with the
	 * database. We assume that exceptions should be rare here i.e. the normal
	 * case is assumed to succeed.
	 */
	private boolean possiblyBroken;
	/** If true, we've called close() on this connection. */
	private boolean logicallyClosed = false;
	/** Original partition. */
	private ConnectionPartition originatingPartition = null;
	/** Prepared Statement Cache. */
	private IStatementCache preparedStatementCache = null;
	/** Prepared Statement Cache. */
	private IStatementCache callableStatementCache = null;
	/** Logger handle. */
	private static Logger logger = LoggerFactory.getLogger(ConnectionHandle.class);
	/** An opaque handle for an application to use in any way it deems fit. */
	private Object debugHandle;

	/**
	 * List of statements that will be closed when this preparedStatement is
	 * logically closed.
	 */
	private ConcurrentLinkedQueue<Statement> statementHandles = new ConcurrentLinkedQueue<Statement>();
	/** Handle to the connection hook as defined in the config. */
	private ConnectionHook connectionHook;
	/** If true, give warnings if application tried to issue a close twice (for debugging only). */
	private boolean doubleCloseCheck;
	/** exception trace if doubleCloseCheck is enabled. */  
	private volatile String doubleCloseException = null;
	/** If true, log sql statements. */
	private boolean logStatementsEnabled;

	/**
	 * From: http://publib.boulder.ibm.com/infocenter/db2luw/v8/index.jsp?topic=/com.ibm.db2.udb.doc/core/r0sttmsg.htm
	 * Table 7. Class Code 08: Connection Exception
		SQLSTATE Value	  
		Value	Meaning
		08001	The application requester is unable to establish the connection.
		08002	The connection already exists.
		08003	The connection does not exist.
		08004	The application server rejected establishment of the connection.
		08007	Transaction resolution unknown.
		08502	The CONNECT statement issued by an application process running with a SYNCPOINT of TWOPHASE has failed, because no transaction manager is available.
		08504	An error was encountered while processing the specified path rename configuration file.
	 SQL Failure codes indicating the database is broken/died (and thus kill off remaining connections). 
	  Anything else will be taken as the *connection* (not the db) being broken. 

	  08S01 is a mysql-specific code to indicate a connection failure (though in my tests it was triggered even when the entire
	  database was down so I treat that as a DB failure)
	 */
	private static final ImmutableSet<String> sqlStateDBFailureCodes = ImmutableSet.of("08001", "08007", "08S01"); 
	/**
	 * Connection wrapper constructor
	 * 
	 * @param url
	 *            JDBC connection string
	 * @param username
	 *            user to use
	 * @param password
	 *            password for db
	 * @param pool
	 *            pool handle.
	 * @throws SQLException
	 *             on error
	 */
	public ConnectionHandle(String url, String username, String password,
			BoneCP pool) throws SQLException {

		boolean tryAgain = false;

		this.pool = pool;
		do{
			try {
				// keep track of this hook.
				this.connectionHook = this.pool.getConfig().getConnectionHook();

				this.connection = DriverManager.getConnection(url, username,
						password);

				this.doubleCloseCheck = pool.getConfig().isCloseConnectionWatch();
				this.logStatementsEnabled = pool.getConfig().isLogStatementsEnabled();
				int cacheSize = pool.getConfig().getPreparedStatementsCacheSize();
				if (cacheSize > 0) {
					this.preparedStatementCache = new StatementCache(cacheSize,  pool.getConfig().getStatementsCachedPerConnection());
					this.callableStatementCache = new StatementCache(cacheSize,  pool.getConfig().getStatementsCachedPerConnection());

				}
				// call the hook, if available.
				if (this.connectionHook != null){
					this.connectionHook.onAcquire(this);
				}

				sendInitSQL();
			} catch (Throwable t) {
				// call the hook, if available.
				if (this.connectionHook != null){
					tryAgain = this.connectionHook.onAcquireFail(t);
				}
				if (!tryAgain){
					throw markPossiblyBroken(t);
				}
			}
		} while (tryAgain);
	}

	/** Sends any configured SQL init statement. 
	 * @throws SQLException on error
	 */
	public void sendInitSQL() throws SQLException {
		// fetch any configured setup sql.
		String initSQL = this.pool.getConfig().getInitSQL();
		if (initSQL != null){
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(initSQL);
			// free up resources 
			if (rs != null){
				rs.close();
			}
			stmt.close();
		}
	}

	/** Private constructor used solely for unit testing. 
	 * @param connection 
	 * @param preparedStatementCache 
	 * @param callableStatementCache 
	 * @param pool */
	public ConnectionHandle(Connection connection, IStatementCache preparedStatementCache, IStatementCache callableStatementCache, BoneCP pool){
		this.connection = connection;
		this.preparedStatementCache = preparedStatementCache;
		this.callableStatementCache = callableStatementCache;
		this.pool = pool;
	}


	/**
	 * Adds the given statement to a list.
	 * 
	 * @param statement
	 *            Statement to keep track of
	 * @return statement
	 */
	private Statement trackStatement(Statement statement) {
		if (statement != null){
			this.statementHandles.add(statement);
		}
		return statement;
	}

	/** 
	 * Given an exception, flag the connection (or database) as being potentially broken. If the exception is a data-specific exception,
	 * do nothing except throw it back to the application. 
	 * 
	 * @param t Throwable exception to process
	 * @return SQLException for further processing
	 */
	protected SQLException markPossiblyBroken(Throwable t) {
		SQLException e;
		if (t instanceof SQLException){
			e=(SQLException) t;
		} else {
			e = new SQLException(t == null ? "Unknown error" : t.getMessage(), "08999");
			e.initCause( t );
		}

		String state = e.getSQLState();
		if (state == null){ // safety;
			state = "Z";
		}
		// use the active key to prevent two connections each triggering a "DB is down" from destroying the database repeatedly
		if (sqlStateDBFailureCodes.contains(state) && this.pool != null){
			logger.error("Database access problem. Killing off all remaining connections in the connection pool. SQL State = " + state);
			this.pool.terminateAllConnections();
		}

		// SQL-92 says:
		//		 Class values that begin with one of the <digit>s '5', '6', '7',
		//         '8', or '9' or one of the <simple Latin upper case letter>s 'I',
		//         'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
		//         'W', 'X', 'Y', or 'Z' are reserved for implementation-specified
		//         conditions.


		char firstChar = state.charAt(0);
		// if it's a communication exception or an implementation-specific error code, flag this connection as being potentially broken.
		if (state.startsWith("08") ||  (firstChar >= '5' && firstChar <='9') || (firstChar >='I' && firstChar <= 'Z')){
			this.possiblyBroken = true;
		}

		return e;
	}


	public void clearWarnings() throws SQLException {
		checkClosed();
		try {
			this.connection.clearWarnings();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	/**
	 * Checks if the connection is (logically) closed and throws an exception if it is.
	 * 
	 * @throws SQLException
	 *             on error
	 * 
	 * 
	 */
	private void checkClosed() throws SQLException {
		if (this.logicallyClosed) {
			throw new SQLException("Connection is closed!");
		}
	}

	/**
	 * Release the connection if called. 
	 * 
	 * @throws SQLException Never really thrown
	 */
	public void close() throws SQLException {
		try {
			if (!this.logicallyClosed) {
				this.logicallyClosed = true;
				this.pool.releaseConnection(this);

				if (this.doubleCloseCheck){
					this.doubleCloseException = this.pool.captureStackTrace(CLOSED_TWICE_EXCEPTION_MESSAGE);
				}
			} else {
				if (this.doubleCloseCheck && this.doubleCloseException != null){
					String currentLocation = this.pool.captureStackTrace("Last closed trace from thread ["+Thread.currentThread().getName()+"]:\n");
					logger.error(String.format(LOG_ERROR_MESSAGE, this.doubleCloseException, currentLocation));
				}
			}
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}


	/**
	 * Close off the connection.
	 * 
	 * @throws SQLException
	 */
	protected void internalClose() throws SQLException {
		try {
			clearStatementHandles(true);
			this.connection.close();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public void commit() throws SQLException {
		checkClosed();
		try {
			this.connection.commit();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public Array createArrayOf(String typeName, Object[] elements)
	throws SQLException {
		Array result = null;
		checkClosed();
		try {
			result = this.connection.createArrayOf(typeName, elements);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}

		return result;
	}

	public Blob createBlob() throws SQLException {
		Blob result = null;
		checkClosed();
		try {
			result = this.connection.createBlob();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public Clob createClob() throws SQLException {
		Clob result = null;
		checkClosed();
		try {
			result = this.connection.createClob();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}

		return result;

	}

	public NClob createNClob() throws SQLException {
		NClob result = null;
		checkClosed();
		try {
			result = this.connection.createNClob();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public SQLXML createSQLXML() throws SQLException {
		SQLXML result = null;
		checkClosed();
		try {
			result = this.connection.createSQLXML();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public Statement createStatement() throws SQLException {
		Statement result = null;
		checkClosed();
		try {
			result = trackStatement(new StatementHandle(this.connection.createStatement(), this, this.logStatementsEnabled));
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
	throws SQLException {
		Statement result = null;
		checkClosed();
		try {
			result = trackStatement(new StatementHandle(this.connection.createStatement(resultSetType, resultSetConcurrency), this, this.logStatementsEnabled));
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
	throws SQLException {
		Statement result = null;
		checkClosed();
		try {
			result = trackStatement(new StatementHandle(this.connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), this, this.logStatementsEnabled));
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}

		return result;
	}

	public Struct createStruct(String typeName, Object[] attributes)
	throws SQLException {
		Struct result = null;
		checkClosed();
		try {
			result = this.connection.createStruct(typeName, attributes);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public boolean getAutoCommit() throws SQLException {
		boolean result = false;
		checkClosed();
		try {
			result = this.connection.getAutoCommit();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public String getCatalog() throws SQLException {
		String result = null;
		checkClosed();
		try {
			result = this.connection.getCatalog();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public Properties getClientInfo() throws SQLException {
		Properties result = null;
		checkClosed();
		try {
			result = this.connection.getClientInfo();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public String getClientInfo(String name) throws SQLException {
		String result = null;
		checkClosed();
		try {
			result = this.connection.getClientInfo(name);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public int getHoldability() throws SQLException {
		int result = 0;
		checkClosed();
		try {
			result = this.connection.getHoldability();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}

		return result;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		DatabaseMetaData result = null;
		checkClosed();
		try {
			result = this.connection.getMetaData();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public int getTransactionIsolation() throws SQLException {
		int result = 0;
		checkClosed();
		try {
			result = this.connection.getTransactionIsolation();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		Map<String, Class<?>> result = null;
		checkClosed();
		try {
			result = this.connection.getTypeMap();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public SQLWarning getWarnings() throws SQLException {
		SQLWarning result = null;
		checkClosed();
		try {
			result = this.connection.getWarnings();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public boolean isClosed() throws SQLException {
		boolean result = false;
		checkClosed();
		try {
			result = this.connection.isClosed();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public boolean isReadOnly() throws SQLException {
		boolean result = false;
		checkClosed();
		try {
			result = this.connection.isReadOnly();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public boolean isValid(int timeout) throws SQLException {
		boolean result = false;
		checkClosed();
		try {
			result = this.connection.isValid(timeout);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public String nativeSQL(String sql) throws SQLException {
		String result = null;
		checkClosed();
		try {
			result = this.connection.nativeSQL(sql);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		CallableStatement result = null;
		checkClosed();
		try {

			if (this.callableStatementCache != null) {
				result = (CallableStatement) this.callableStatementCache.get(sql);
				if (result == null) {
					String cacheKey = sql;
					result = (CallableStatement) trackStatement(new CallableStatementHandle(this.connection.prepareCall(sql),
							sql, this.callableStatementCache, this, cacheKey, this.logStatementsEnabled));

				} 

				((CallableStatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareCall(sql);
			}

		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		CallableStatement result = null;
		checkClosed();
		try {
			if (this.callableStatementCache != null) {
				result = (CallableStatement) this.callableStatementCache.get(sql, resultSetType, resultSetConcurrency);

				if (result == null) {
					String cacheKey = this.callableStatementCache.calculateCacheKey(sql, resultSetType, resultSetConcurrency);
					result = (CallableStatement) trackStatement(new CallableStatementHandle(
							this.connection.prepareCall(sql, resultSetType, resultSetConcurrency), sql, this.callableStatementCache, this, cacheKey, this.logStatementsEnabled));

				} 

				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareCall(sql, resultSetType, resultSetConcurrency);
			}

		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
	throws SQLException {
		CallableStatement result = null;
		checkClosed();
		try {
			if (this.callableStatementCache != null) {

				result = (CallableStatement) this.callableStatementCache.get(sql);

				if (result == null) {
					result = (CallableStatement) trackStatement(new CallableStatementHandle(
							this.connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql,
							this.callableStatementCache, this, sql,this.logStatementsEnabled));

				} 
				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareCall(sql, resultSetType,
						resultSetConcurrency, resultSetHoldability);
			}
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		Statement result = null;
		checkClosed();
		try {
			if (this.preparedStatementCache != null) {

				result = this.preparedStatementCache.get(sql);

				if (result == null) {

					String cacheKey = sql;

					result =  trackStatement(new PreparedStatementHandle(
							this.connection.prepareStatement(sql), sql,
							this.preparedStatementCache, this, cacheKey,this.logStatementsEnabled));
				} 
				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareStatement(sql);
			}
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return (PreparedStatement) result;
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
	throws SQLException {
		PreparedStatement result = null;
		checkClosed();
		try {
			if (this.preparedStatementCache != null) {

				result = (PreparedStatement) this.preparedStatementCache.get(sql, autoGeneratedKeys);

				if (result == null) {
					String cacheKey = this.preparedStatementCache.calculateCacheKey(sql, autoGeneratedKeys);

					result = (PreparedStatement) trackStatement(new PreparedStatementHandle(
							this.connection.prepareStatement(sql,
									autoGeneratedKeys), sql,
									this.preparedStatementCache, this, cacheKey, this.logStatementsEnabled));
				} 
				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareStatement(sql,
						autoGeneratedKeys);
			}

		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
	throws SQLException {
		PreparedStatement result = null;
		checkClosed();
		try {
			if (this.preparedStatementCache != null) {

				result = (PreparedStatement) this.preparedStatementCache.get(sql, columnIndexes);

				if (result == null) {
					String cacheKey = this.preparedStatementCache.calculateCacheKey(sql, columnIndexes);

					result = (PreparedStatement) trackStatement(new PreparedStatementHandle(
							this.connection
							.prepareStatement(sql, columnIndexes), sql,
							this.preparedStatementCache, this, cacheKey, this.logStatementsEnabled));
				}
				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareStatement(sql, columnIndexes);
			}
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
	throws SQLException {
		PreparedStatement result = null;
		checkClosed();
		try {
			if (this.preparedStatementCache != null) {

				result = (PreparedStatement) this.preparedStatementCache.get(sql, columnNames);

				if (result == null) {
					String cacheKey = this.preparedStatementCache.calculateCacheKey(sql, columnNames);

					result = (PreparedStatement) trackStatement(new PreparedStatementHandle(
							this.connection.prepareStatement(sql, columnNames),
							sql, this.preparedStatementCache, this, cacheKey, this.logStatementsEnabled));

				} 
				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareStatement(sql, columnNames);
			}

		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		PreparedStatement result = null;
		checkClosed();
		try {
			if (this.preparedStatementCache != null) {

				result = (PreparedStatement) this.preparedStatementCache.get(sql, resultSetType, resultSetConcurrency);

				if (result == null) {
					String cacheKey = this.preparedStatementCache.calculateCacheKey(sql, resultSetType, resultSetConcurrency);

					result = (PreparedStatement) trackStatement(new PreparedStatementHandle(
							this.connection.prepareStatement(sql,
									resultSetType, resultSetConcurrency), sql,
									this.preparedStatementCache, this, cacheKey, this.logStatementsEnabled));
				} 
				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareStatement(sql, resultSetType,
						resultSetConcurrency);
			}

		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
	throws SQLException {
		PreparedStatement result = null;
		checkClosed();
		try {
			if (this.preparedStatementCache != null) {

				result = (PreparedStatement) this.preparedStatementCache.get(sql, resultSetType, resultSetConcurrency, resultSetHoldability);

				if (result == null) {
					String cacheKey = this.preparedStatementCache.calculateCacheKey(sql, resultSetType, resultSetConcurrency, resultSetHoldability);

					result = (PreparedStatement) trackStatement(new PreparedStatementHandle(
							this.connection.prepareStatement(sql,
									resultSetType, resultSetConcurrency,
									resultSetHoldability), sql,
									this.preparedStatementCache, this, cacheKey, this.logStatementsEnabled));

				} 
				((StatementHandle)result).setLogicallyOpen();
			} else {
				result = this.connection.prepareStatement(sql, resultSetType,
						resultSetConcurrency, resultSetHoldability);
			}

		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		checkClosed();
		try {
			this.connection.releaseSavepoint(savepoint);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);

		}
	}

	public void rollback() throws SQLException {
		checkClosed();
		try {
			this.connection.rollback();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		checkClosed();
		try {
			this.connection.rollback(savepoint);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		checkClosed();
		try {
			this.connection.setAutoCommit(autoCommit);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public void setCatalog(String catalog) throws SQLException {
		checkClosed();
		try {
			this.connection.setCatalog(catalog);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public void setClientInfo(Properties properties)
	throws SQLClientInfoException {
		this.connection.setClientInfo(properties);
	}

	public void setClientInfo(String name, String value)
	throws SQLClientInfoException {
		this.connection.setClientInfo(name, value);
	}

	public void setHoldability(int holdability) throws SQLException {
		checkClosed();
		try {
			this.connection.setHoldability(holdability);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		checkClosed();
		try {
			this.connection.setReadOnly(readOnly);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public Savepoint setSavepoint() throws SQLException {
		checkClosed();
		Savepoint result = null;
		try {
			result = this.connection.setSavepoint();
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		checkClosed();
		Savepoint result = null;
		try {
			result = this.connection.setSavepoint(name);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
		return result;
	}

	public void setTransactionIsolation(int level) throws SQLException {
		checkClosed();
		try {
			this.connection.setTransactionIsolation(level);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		checkClosed();
		try {
			this.connection.setTypeMap(map);
		} catch (Throwable t) {
			throw markPossiblyBroken(t);
		}
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.connection.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.connection.unwrap(iface);
	}

	/**
	 * @return the connectionLastUsed
	 */
	public long getConnectionLastUsed() {
		return this.connectionLastUsed;
	}

	/**
	 * @param connectionLastUsed
	 *            the connectionLastUsed to set
	 */
	protected void setConnectionLastUsed(long connectionLastUsed) {
		this.connectionLastUsed = connectionLastUsed;
	}

	/**
	 * @return the connectionLastReset
	 */
	public long getConnectionLastReset() {
		return this.connectionLastReset;
	}

	/**
	 * @param connectionLastReset
	 *            the connectionLastReset to set
	 */
	protected void setConnectionLastReset(long connectionLastReset) {
		this.connectionLastReset = connectionLastReset;
	}

	/**
	 * Gets true if connection has triggered an exception at some point.
	 * 
	 * @return true if the connection has triggered an error
	 */
	public boolean isPossiblyBroken() {
		return this.possiblyBroken;
	}


	/**
	 * Gets the partition this came from.
	 * 
	 * @return the partition this came from
	 */
	public ConnectionPartition getOriginatingPartition() {
		return this.originatingPartition;
	}

	/**
	 * Sets Originating partition
	 * 
	 * @param originatingPartition
	 *            to set
	 */
	protected void setOriginatingPartition(ConnectionPartition originatingPartition) {
		this.originatingPartition = originatingPartition;
	}

	/**
	 * Renews this connection, i.e. Sets this connection to be logically open
	 * (although it was never really physically closed)
	 */
	protected void renewConnection() {
		this.logicallyClosed = false;
		if (this.doubleCloseCheck){
			this.doubleCloseException = null;
		}
	}


	/** Clears out the statement handles.
	 * @param internalClose if true, close the inner statement handle too. 
	 * @throws SQLException
	 */
	protected void clearStatementHandles(boolean internalClose) throws SQLException {
		Statement statement = null;
		while ((statement = this.statementHandles.poll()) != null) {
			if (internalClose){
				((StatementHandle) statement).internalClose();
			} else {
				((StatementHandle) statement).close();
			}
		}
	}

	/** Returns a debug handle as previously set by an application
	 * @return DebugHandle
	 */
	public Object getDebugHandle() {
		return this.debugHandle;
	}

	/** Sets a debugHandle, an object that is not used by the connection pool at all but may be set by an application to track
	 * this particular connection handle for any purpose it deems fit.
	 * @param debugHandle any object.
	 */
	public void setDebugHandle(Object debugHandle) {
		this.debugHandle = debugHandle;
	}

	/** Returns the internal connection as obtained via the JDBC driver.
	 * @return the raw connection
	 */
	public Connection getRawConnection() {
		return this.connection;
	}

	/** Returns the configured connection hook object.
	 * @return the connectionHook that was set in the config
	 */
	public ConnectionHook getConnectionHook() {
		return this.connectionHook;
	}

	/** Returns true if this connection has been logically closed.
	 * @return the logicallyClosed setting.
	 */
	public boolean isLogicallyClosed() {
		return this.logicallyClosed;
	}
}
