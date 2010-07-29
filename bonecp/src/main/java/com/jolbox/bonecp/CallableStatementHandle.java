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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
// #ifdef JDK6
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLXML;
// #endif JDK6 
/**
 * Wrapper around CallableStatement.
 * 
 * @author Wallace
 * 
 */
public class CallableStatementHandle extends PreparedStatementHandle implements
		CallableStatement {
	/** Handle to statement. */
	private CallableStatement internalCallableStatement;

	/**
	 * CallableStatement constructor
	 * 
	 * @param internalCallableStatement
	 * @param sql
	 * @param cache
	 * @param connectionHandle
	 * @param cacheKey key to cache
	 */
	public CallableStatementHandle(CallableStatement internalCallableStatement,
			String sql, ConnectionHandle connectionHandle, String cacheKey, IStatementCache cache) {
		super(internalCallableStatement, sql, connectionHandle, cacheKey, cache);
		this.internalCallableStatement = internalCallableStatement;
		this.connectionHandle = connectionHandle;
		this.sql = sql;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getArray(int)
	 */
	// @Override
	public Array getArray(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getArray(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getArray(java.lang.String)
	 */
	// @Override
	public Array getArray(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getArray(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBigDecimal(int)
	 */
	// @Override
	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBigDecimal(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBigDecimal(java.lang.String)
	 */
	// @Override
	public BigDecimal getBigDecimal(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBigDecimal(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBigDecimal(int, int)
	 */
	// @Override
	@Deprecated
	public BigDecimal getBigDecimal(int parameterIndex, int scale)
			throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBigDecimal(parameterIndex,
					scale);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBlob(int)
	 */
	// @Override
	public Blob getBlob(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBlob(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBlob(java.lang.String)
	 */
	// @Override
	public Blob getBlob(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBlob(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBoolean(int)
	 */
	// @Override
	public boolean getBoolean(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBoolean(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBoolean(java.lang.String)
	 */
	// @Override
	public boolean getBoolean(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBoolean(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getByte(int)
	 */
	// @Override
	public byte getByte(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getByte(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getByte(java.lang.String)
	 */
	// @Override
	public byte getByte(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getByte(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBytes(int)
	 */
	// @Override
	public byte[] getBytes(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBytes(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getBytes(java.lang.String)
	 */
	// @Override
	public byte[] getBytes(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getBytes(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	// #ifdef JDK6
	@Override
	public Reader getCharacterStream(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getCharacterStream(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}
	
	@Override
	public Reader getCharacterStream(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getCharacterStream(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}
	
	@Override
	public Reader getNCharacterStream(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement
					.getNCharacterStream(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public Reader getNCharacterStream(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement
					.getNCharacterStream(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public NClob getNClob(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getNClob(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public NClob getNClob(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getNClob(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public String getNString(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getNString(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public String getNString(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getNString(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}
@Override
	public RowId getRowId(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getRowId(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public RowId getRowId(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getRowId(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public SQLXML getSQLXML(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getSQLXML(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public SQLXML getSQLXML(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getSQLXML(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}


	@Override
	public void setAsciiStream(String parameterName, InputStream x)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setAsciiStream(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}
	@Override
	public void setAsciiStream(String parameterName, InputStream x, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setAsciiStream(parameterName, x, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setBinaryStream(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}
	
		@Override
		public void setBinaryStream(String parameterName, InputStream x, long length)
				throws SQLException {
			checkClosed();
			try {
				this.internalCallableStatement.setBinaryStream(parameterName, x, length);
				if (this.logStatementsEnabled){
					this.logParams.put(parameterName, x);
				}
			} catch (Throwable t) {
				throw this.connectionHandle.markPossiblyBroken(t);
				
			}

		}

		@Override
		public void setBlob(String parameterName, Blob x) throws SQLException {
			checkClosed();
			try {
				this.internalCallableStatement.setBlob(parameterName, x);
				if (this.logStatementsEnabled){
					this.logParams.put(parameterName, x);
				}
			} catch (Throwable t) {
				throw this.connectionHandle.markPossiblyBroken(t);
				
			}

		}

		@Override
		public void setBlob(String parameterName, InputStream inputStream)
				throws SQLException {
			checkClosed();
			try {
				this.internalCallableStatement.setBlob(parameterName, inputStream);
				if (this.logStatementsEnabled){
					this.logParams.put(parameterName, inputStream);
				}
			} catch (Throwable t) {
				throw this.connectionHandle.markPossiblyBroken(t);
				
			}

		}

		@Override
		public void setBlob(String parameterName, InputStream inputStream,
				long length) throws SQLException {
			checkClosed();
			try {
				this.internalCallableStatement.setBlob(parameterName, inputStream, length);
				if (this.logStatementsEnabled){
					this.logParams.put(parameterName, inputStream);
				}
			} catch (Throwable t) {
				throw this.connectionHandle.markPossiblyBroken(t);
				
			}

		}


	@Override
	public void setCharacterStream(String parameterName, Reader reader)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setCharacterStream(parameterName, reader);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, reader);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader,
			long length) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setCharacterStream(parameterName, reader, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, reader);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setClob(String parameterName, Clob x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setClob(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setClob(String parameterName, Reader reader)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setClob(parameterName, reader);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, reader);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setClob(String parameterName, Reader reader, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setClob(parameterName, reader, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, reader);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}
	@Override
	public void setNCharacterStream(String parameterName, Reader value)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNCharacterStream(parameterName, value);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, value);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setNCharacterStream(String parameterName, Reader value,
			long length) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNCharacterStream(parameterName, value, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, value);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setNClob(String parameterName, NClob value) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNClob(parameterName, value);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, value);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setNClob(String parameterName, Reader reader)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNClob(parameterName, reader);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, reader);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setNClob(String parameterName, Reader reader, long length)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNClob(parameterName, reader, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, reader);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	@Override
	public void setNString(String parameterName, String value)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNString(parameterName, value);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, value);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}
	
	@Override
	public void setRowId(String parameterName, RowId x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setRowId(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	@Override
	public void setSQLXML(String parameterName, SQLXML xmlObject)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setSQLXML(parameterName, xmlObject);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, xmlObject);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

 	// #endif JDK6 

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getClob(int)
	 */
	// @Override
	public Clob getClob(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getClob(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getClob(java.lang.String)
	 */
	// @Override
	public Clob getClob(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getClob(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getDate(int)
	 */
	// @Override
	public Date getDate(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getDate(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getDate(java.lang.String)
	 */
	// @Override
	public Date getDate(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getDate(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getDate(int, java.util.Calendar)
	 */
	// @Override
	public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getDate(parameterIndex, cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getDate(java.lang.String,
	 *      java.util.Calendar)
	 */
	// @Override
	public Date getDate(String parameterName, Calendar cal) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getDate(parameterName, cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getDouble(int)
	 */
	// @Override
	public double getDouble(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getDouble(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getDouble(java.lang.String)
	 */
	// @Override
	public double getDouble(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getDouble(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getFloat(int)
	 */
	// @Override
	public float getFloat(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getFloat(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getFloat(java.lang.String)
	 */
	// @Override
	public float getFloat(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getFloat(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getInt(int)
	 */
	// @Override
	public int getInt(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getInt(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getInt(java.lang.String)
	 */
	// @Override
	public int getInt(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getInt(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getLong(int)
	 */
	// @Override
	public long getLong(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getLong(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getLong(java.lang.String)
	 */
	// @Override
	public long getLong(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getLong(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}


	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getObject(int)
	 */
	// @Override
	public Object getObject(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getObject(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getObject(java.lang.String)
	 */
	// @Override
	public Object getObject(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getObject(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getObject(int, java.util.Map)
	 */
	// @Override
	public Object getObject(int parameterIndex, Map<String, Class<?>> map)
			throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getObject(parameterIndex, map);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getObject(java.lang.String,
	 *      java.util.Map)
	 */
	// @Override
	public Object getObject(String parameterName, Map<String, Class<?>> map)
			throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getObject(parameterName, map);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getRef(int)
	 */
	// @Override
	public Ref getRef(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getRef(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getRef(java.lang.String)
	 */
	// @Override
	public Ref getRef(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getRef(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	
	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getShort(int)
	 */
	// @Override
	public short getShort(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getShort(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getShort(java.lang.String)
	 */
	// @Override
	public short getShort(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getShort(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getString(int)
	 */
	// @Override
	public String getString(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getString(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getString(java.lang.String)
	 */
	// @Override
	public String getString(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getString(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTime(int)
	 */
	// @Override
	public Time getTime(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTime(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTime(java.lang.String)
	 */
	// @Override
	public Time getTime(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTime(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTime(int, java.util.Calendar)
	 */
	// @Override
	public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTime(parameterIndex, cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTime(java.lang.String,
	 *      java.util.Calendar)
	 */
	// @Override
	public Time getTime(String parameterName, Calendar cal) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTime(parameterName, cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTimestamp(int)
	 */
	// @Override
	public Timestamp getTimestamp(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTimestamp(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTimestamp(java.lang.String)
	 */
	// @Override
	public Timestamp getTimestamp(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTimestamp(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTimestamp(int, java.util.Calendar)
	 */
	// @Override
	public Timestamp getTimestamp(int parameterIndex, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTimestamp(parameterIndex,
					cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getTimestamp(java.lang.String,
	 *      java.util.Calendar)
	 */
	// @Override
	public Timestamp getTimestamp(String parameterName, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getTimestamp(parameterName,
					cal);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getURL(int)
	 */
	// @Override
	public URL getURL(int parameterIndex) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getURL(parameterIndex);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#getURL(java.lang.String)
	 */
	// @Override
	public URL getURL(String parameterName) throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.getURL(parameterName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#registerOutParameter(int, int)
	 */
	// @Override
	public void registerOutParameter(int parameterIndex, int sqlType)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.registerOutParameter(parameterIndex, sqlType);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String,
	 *      int)
	 */
	// @Override
	public void registerOutParameter(String parameterName, int sqlType)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.registerOutParameter(parameterName, sqlType);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#registerOutParameter(int, int, int)
	 */
	// @Override
	public void registerOutParameter(int parameterIndex, int sqlType, int scale)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.registerOutParameter(parameterIndex, sqlType, scale);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#registerOutParameter(int, int,
	 *      java.lang.String)
	 */
	// @Override
	public void registerOutParameter(int parameterIndex, int sqlType,
			String typeName) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.registerOutParameter(parameterIndex, sqlType, typeName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String,
	 *      int, int)
	 */
	// @Override
	public void registerOutParameter(String parameterName, int sqlType,
			int scale) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.registerOutParameter(parameterName, sqlType, scale);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String,
	 *      int, java.lang.String)
	 */
	// @Override
	public void registerOutParameter(String parameterName, int sqlType,
			String typeName) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.registerOutParameter(parameterName, sqlType, typeName);
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setAsciiStream(java.lang.String,
	 *      java.io.InputStream, int)
	 */
	// @Override
	public void setAsciiStream(String parameterName, InputStream x, int length)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setAsciiStream(parameterName, x, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	
	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setBigDecimal(java.lang.String,
	 *      java.math.BigDecimal)
	 */
	// @Override
	public void setBigDecimal(String parameterName, BigDecimal x)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setBigDecimal(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	
	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setBinaryStream(java.lang.String,
	 *      java.io.InputStream, int)
	 */
	// @Override
	public void setBinaryStream(String parameterName, InputStream x, int length)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setBinaryStream(parameterName, x, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}


	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setBoolean(java.lang.String, boolean)
	 */
	public void setBoolean(String parameterName, boolean x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setBoolean(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setByte(java.lang.String, byte)
	 */
	// @Override
	public void setByte(String parameterName, byte x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setByte(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setBytes(java.lang.String, byte[])
	 */
	// @Override
	public void setBytes(String parameterName, byte[] x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setBytes(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	
	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setCharacterStream(java.lang.String,
	 *      java.io.Reader, int)
	 */
	// @Override
	public void setCharacterStream(String parameterName, Reader reader,
			int length) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setCharacterStream(parameterName, reader, length);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, reader);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date)
	 */
	// @Override
	public void setDate(String parameterName, Date x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setDate(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date,
	 *      java.util.Calendar)
	 */
	// @Override
	public void setDate(String parameterName, Date x, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setDate(parameterName, x, cal);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, PoolUtil.safePrint(x, ", cal=", cal));
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setDouble(java.lang.String, double)
	 */
	// @Override
	public void setDouble(String parameterName, double x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setDouble(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setFloat(java.lang.String, float)
	 */
	// @Override
	public void setFloat(String parameterName, float x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setFloat(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setInt(java.lang.String, int)
	 */
	// @Override
	public void setInt(String parameterName, int x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setInt(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setLong(java.lang.String, long)
	 */
	// @Override
	public void setLong(String parameterName, long x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setLong(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}


	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setNull(java.lang.String, int)
	 */
	public void setNull(String parameterName, int sqlType) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNull(parameterName, sqlType);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, PoolUtil.safePrint("[SQL NULL type ", sqlType, "]"));
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setNull(java.lang.String, int,
	 *      java.lang.String)
	 */
	// @Override
	public void setNull(String parameterName, int sqlType, String typeName)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setNull(parameterName, sqlType, typeName);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, PoolUtil.safePrint("[SQL NULL type ", sqlType, ", type=", typeName+"]"));
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setObject(java.lang.String,
	 *      java.lang.Object)
	 */
	// @Override
	public void setObject(String parameterName, Object x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setObject(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setObject(java.lang.String,
	 *      java.lang.Object, int)
	 */
	// @Override
	public void setObject(String parameterName, Object x, int targetSqlType)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setObject(parameterName, x, targetSqlType);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setObject(java.lang.String,
	 *      java.lang.Object, int, int)
	 */
	// @Override
	public void setObject(String parameterName, Object x, int targetSqlType,
			int scale) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setObject(parameterName, x, targetSqlType, scale);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	
	public void setShort(String parameterName, short x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setShort(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setString(java.lang.String,
	 *      java.lang.String)
	 */
	// @Override
	public void setString(String parameterName, String x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setString(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time)
	 */
	// @Override
	public void setTime(String parameterName, Time x) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setTime(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time,
	 *      java.util.Calendar)
	 */
	// @Override
	public void setTime(String parameterName, Time x, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setTime(parameterName, x, cal);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, PoolUtil.safePrint(x, ", cal=", cal));
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setTimestamp(java.lang.String,
	 *      java.sql.Timestamp)
	 */
	// @Override
	public void setTimestamp(String parameterName, Timestamp x)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setTimestamp(parameterName, x);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, x);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setTimestamp(java.lang.String,
	 *      java.sql.Timestamp, java.util.Calendar)
	 */
	// @Override
	public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
			throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setTimestamp(parameterName, x, cal);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, PoolUtil.safePrint(x, ", cal=", cal));
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#setURL(java.lang.String, java.net.URL)
	 */
	// @Override
	public void setURL(String parameterName, URL val) throws SQLException {
		checkClosed();
		try {
			this.internalCallableStatement.setURL(parameterName, val);
			if (this.logStatementsEnabled){
				this.logParams.put(parameterName, val);
			}
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.sql.CallableStatement#wasNull()
	 */
	// @Override
	public boolean wasNull() throws SQLException {
		checkClosed();
		try {
			return this.internalCallableStatement.wasNull();
		} catch (Throwable t) {
			throw this.connectionHandle.markPossiblyBroken(t);
			
		}
	}

	/** Returns the callable statement that this wrapper wraps.
	 * @return the internalCallableStatement currently being used.
	 */
	public CallableStatement getInternalCallableStatement() {
		return this.internalCallableStatement;
	}

	/** Sets the callable statement used by this wrapper.
	 * @param internalCallableStatement the internalCallableStatement to set
	 */
	public void setInternalCallableStatement(
			CallableStatement internalCallableStatement) {
		this.internalCallableStatement = internalCallableStatement;
	}
}