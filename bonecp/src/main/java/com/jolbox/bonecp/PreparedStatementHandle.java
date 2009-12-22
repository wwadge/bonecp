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
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 
 * 
 * @author wallacew
 * @version $Revision$
 */
public class PreparedStatementHandle extends StatementHandle implements
		PreparedStatement {

	/** Handle to the real prepared statement. */
	private PreparedStatement internalPreparedStatement;

	/**
	 * PreparedStatement Wrapper constructor.
	 * 
	 * @param internalPreparedStatement
	 * @param sql
	 *            sql statement
	 * @param cache
	 *            cache handle.
	 * @param connectionHandle
	 *            Handle to the connection this is tied to.
	 * @param cacheKey 
	 */
	public PreparedStatementHandle(PreparedStatement internalPreparedStatement,
			String sql, IStatementCache cache, ConnectionHandle connectionHandle, String cacheKey) {
		super(internalPreparedStatement, sql, cache, connectionHandle, cacheKey);
		this.internalPreparedStatement = internalPreparedStatement;
		this.connectionHandle = connectionHandle;
		this.sql = sql;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#addBatch()
	 */
	@Override
	public void addBatch() throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.addBatch();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#clearParameters()
	 */
	@Override
	public void clearParameters() throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.clearParameters();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#execute()
	 */
	@Override
	public boolean execute() throws SQLException {
		checkClosed();
		try {
			return this.internalPreparedStatement.execute();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#executeQuery()
	 */
	@Override
	public ResultSet executeQuery() throws SQLException {
		checkClosed();
		try {
			return this.internalPreparedStatement.executeQuery();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#executeUpdate()
	 */
	@Override
	public int executeUpdate() throws SQLException {
		checkClosed();
		try {
			return this.internalPreparedStatement.executeUpdate();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#getMetaData()
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		checkClosed();
		try {
			return this.internalPreparedStatement.getMetaData();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#getParameterMetaData()
	 */
	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		checkClosed();
		try {
			return this.internalPreparedStatement.getParameterMetaData();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	 */
	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setArray(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setAsciiStream(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,
	 *      int)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setAsciiStream(parameterIndex, x,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,
	 *      long)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setAsciiStream(parameterIndex, x,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBigDecimal(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBinaryStream(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
	 *      int)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBinaryStream(parameterIndex, x,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
	 *      long)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBinaryStream(parameterIndex, x,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 */
	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBlob(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	 */
	@Override
	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBlob(parameterIndex, inputStream);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBlob(parameterIndex, inputStream,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	 */
	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBoolean(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setByte(int, byte)
	 */
	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setByte(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
	 */
	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setBytes(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setCharacterStream(parameterIndex,
					reader);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
	 *      int)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setCharacterStream(parameterIndex,
					reader, length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
	 *      long)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setCharacterStream(parameterIndex,
					reader, length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setClob(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	 */
	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setClob(parameterIndex, reader);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	@Override
	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setClob(parameterIndex, reader,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	 */
	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setDate(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,
	 *      java.util.Calendar)
	 */
	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setDate(parameterIndex, x, cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setDouble(int, double)
	 */
	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setDouble(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setFloat(int, float)
	 */
	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setFloat(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setInt(int, int)
	 */
	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setInt(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setLong(int, long)
	 */
	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setLong(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNCharacterStream(parameterIndex,
					value);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader,
	 *      long)
	 */
	@Override
	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNCharacterStream(parameterIndex,
					value, length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	 */
	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNClob(parameterIndex, value);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	 */
	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNClob(parameterIndex, reader);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	 */
	@Override
	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNClob(parameterIndex, reader,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	 */
	@Override
	public void setNString(int parameterIndex, String value)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNString(parameterIndex, value);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNull(int, int)
	 */
	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNull(parameterIndex, sqlType);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	 */
	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setNull(parameterIndex, sqlType,
					typeName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	 */
	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setObject(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	 */
	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setObject(parameterIndex, x,
					targetSqlType);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int,
	 *      int)
	 */
	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setObject(parameterIndex, x,
					targetSqlType, scaleOrLength);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	 */
	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setRef(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	 */
	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setRowId(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setSQLXML(parameterIndex, xmlObject);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setShort(int, short)
	 */
	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setShort(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	 */
	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setString(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	 */
	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setTime(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,
	 *      java.util.Calendar)
	 */
	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setTime(parameterIndex, x, cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setTimestamp(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,
	 *      java.util.Calendar)
	 */
	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setTimestamp(parameterIndex, x, cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	 */
	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setURL(parameterIndex, x);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.PreparedStatement#setUnicodeStream(int,
	 *      java.io.InputStream, int)
	 */
	@Override
	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		checkClosed();
		try {
			this.internalPreparedStatement.setUnicodeStream(parameterIndex, x,
					length);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
		}

	}

}
