/**
 * Copyright 2009 Wallace Wadge
 *
 * This file is part of BoneCP.
 *
 * BoneCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BoneCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package com.jolbox.benchmark;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * @author Wallace
 *
 */
public class MockPreparedStatement implements PreparedStatement {

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#addBatch()
	 */
	@Override
	public void addBatch() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#clearParameters()
	 */
	@Override
	public void clearParameters() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#execute()
	 */
	@Override
	public boolean execute() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#executeQuery()
	 */
	@Override
	public ResultSet executeQuery() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#executeUpdate()
	 */
	@Override
	public int executeUpdate() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#getMetaData()
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#getParameterMetaData()
	 */
	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	 */
	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 */
	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	 */
	@Override
	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	 */
	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setByte(int, byte)
	 */
	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
	 */
	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	 */
	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	@Override
	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	 */
	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
	 */
	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setDouble(int, double)
	 */
	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setFloat(int, float)
	 */
	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setInt(int, int)
	 */
	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setLong(int, long)
	 */
	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	 */
	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	 */
	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	 */
	@Override
	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	 */
	@Override
	public void setNString(int parameterIndex, String value)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNull(int, int)
	 */
	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	 */
	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	 */
	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	 */
	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
	 */
	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	 */
	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	 */
	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setShort(int, short)
	 */
	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	 */
	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	 */
	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
	 */
	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
	 */
	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	 */
	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	@Override
	public void addBatch(String sql) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#cancel()
	 */
	@Override
	public void cancel() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#clearBatch()
	 */
	@Override
	public void clearBatch() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#close()
	 */
	@Override
	public void close() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#execute(java.lang.String)
	 */
	@Override
	public boolean execute(String sql) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	@Override
	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#executeBatch()
	 */
	@Override
	public int[] executeBatch() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#executeQuery(java.lang.String)
	 */
	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#executeUpdate(java.lang.String)
	 */
	@Override
	public int executeUpdate(String sql) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	@Override
	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	@Override
	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getFetchDirection()
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getFetchSize()
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	@Override
	public int getMaxFieldSize() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getMaxRows()
	 */
	@Override
	public int getMaxRows() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getMoreResults()
	 */
	@Override
	public boolean getMoreResults() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	@Override
	public boolean getMoreResults(int current) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	@Override
	public int getQueryTimeout() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getResultSet()
	 */
	@Override
	public ResultSet getResultSet() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getResultSetConcurrency()
	 */
	@Override
	public int getResultSetConcurrency() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	@Override
	public int getResultSetHoldability() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getResultSetType()
	 */
	@Override
	public int getResultSetType() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getUpdateCount()
	 */
	@Override
	public int getUpdateCount() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#isPoolable()
	 */
	@Override
	public boolean isPoolable() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setCursorName(java.lang.String)
	 */
	@Override
	public void setCursorName(String name) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setEscapeProcessing(boolean)
	 */
	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	@Override
	public void setFetchDirection(int direction) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	@Override
	public void setFetchSize(int rows) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	@Override
	public void setMaxFieldSize(int max) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	@Override
	public void setMaxRows(int max) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setPoolable(boolean)
	 */
	@Override
	public void setPoolable(boolean poolable) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

}
