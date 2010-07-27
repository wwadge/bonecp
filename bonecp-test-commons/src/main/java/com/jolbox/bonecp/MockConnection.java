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
package com.jolbox.bonecp;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

/**
 * @author Wallace
 *
 */
@SuppressWarnings("all")
public class MockConnection implements Connection {

	/** {@inheritDoc}
	 * @see java.sql.Connection#clearWarnings()
	 */
	// @Override
	public void clearWarnings() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#close()
	 */
	// @Override
	public void close() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#commit()
	 */
	// @Override
	public void commit() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	// @Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#createBlob()
	 */
	// @Override
	public Blob createBlob() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#createClob()
	 */
	// @Override
	public Clob createClob() throws SQLException {
		return null;
	}

	// #ifdef JDK6
	@Override
	public NClob createNClob() throws SQLException {
		return null;
	}

	public SQLXML createSQLXML() throws SQLException {
		return null;
	}
	// #endif JDK6

	/** {@inheritDoc}
	 * @see java.sql.Connection#createStatement()
	 */
	// @Override
	public Statement createStatement() throws SQLException {
		return new MockJDBCStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	// @Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	// @Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	// @Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getAutoCommit()
	 */
	// @Override
	public boolean getAutoCommit() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getCatalog()
	 */
	// @Override
	public String getCatalog() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getClientInfo()
	 */
	// @Override
	public Properties getClientInfo() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	// @Override
	public String getClientInfo(String name) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getHoldability()
	 */
	// @Override
	public int getHoldability() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getMetaData()
	 */
	// @Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	// @Override
	public int getTransactionIsolation() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getTypeMap()
	 */
	// @Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#getWarnings()
	 */
	// @Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#isClosed()
	 */
	// @Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#isReadOnly()
	 */
	// @Override
	public boolean isReadOnly() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#isValid(int)
	 */
	// @Override
	public boolean isValid(int timeout) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	// @Override
	public String nativeSQL(String sql) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	// @Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return new MockCallableStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	// @Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return new MockCallableStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	// @Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new MockCallableStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	// @Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new MockPreparedStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	// @Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return new MockPreparedStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	// @Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return new MockPreparedStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	// @Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return new MockPreparedStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	// @Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return new MockPreparedStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	// @Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new MockPreparedStatement();
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	// @Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#rollback()
	 */
	// @Override
	public void rollback() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	// @Override
	public void rollback(Savepoint savepoint) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	// @Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	// @Override
	public void setCatalog(String catalog) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	// @Override
	public void setClientInfo(Properties properties) {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
	 */
	// @Override
	public void setClientInfo(String name, String value) {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setHoldability(int)
	 */
	// @Override
	public void setHoldability(int holdability) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	// @Override
	public void setReadOnly(boolean readOnly) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setSavepoint()
	 */
	// @Override
	public Savepoint setSavepoint() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	// @Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	// @Override
	public void setTransactionIsolation(int level) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	// @Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	// @Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	// @Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

}