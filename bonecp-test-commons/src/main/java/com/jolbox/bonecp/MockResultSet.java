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

/**
 * 
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
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
// #ifdef JDK>6
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLXML;
// #endif JDK>6 
/**
 * @author Wallace
 *
 */
@SuppressWarnings("all")
public class MockResultSet implements ResultSet {

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#absolute(int)
	 */
	public boolean absolute(int row) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#afterLast()
	 */
	public void afterLast() throws SQLException {
		// do nothing
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	public void beforeFirst() throws SQLException {
		// do nothing
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	public void cancelRowUpdates() throws SQLException {
		// do nothing

	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		// do nothing
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#close()
	 */
	public void close() throws SQLException {
		// do nothing
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#deleteRow()
	 */
	public void deleteRow() throws SQLException {
		// do nothing
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	public int findColumn(String columnLabel) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#first()
	 */
	public boolean first() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getArray(int)
	 */
	public Array getArray(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	public Array getArray(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
	 */
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 */
	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
	 */
	public BigDecimal getBigDecimal(String columnLabel, int scale)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
	 */
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	public Blob getBlob(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBlob(java.lang.String)
	 */
	public Blob getBlob(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	public boolean getBoolean(int columnIndex) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String columnLabel) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getByte(int)
	 */
	public byte getByte(int columnIndex) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	public byte getByte(String columnLabel) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	public byte[] getBytes(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getBytes(java.lang.String)
	 */
	public byte[] getBytes(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
	 */
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getClob(int)
	 */
	public Clob getClob(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getClob(java.lang.String)
	 */
	public Clob getClob(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getConcurrency()
	 */
	public int getConcurrency() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getCursorName()
	 */
	public String getCursorName() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	public Date getDate(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	public double getDouble(int columnIndex) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	public double getDouble(String columnLabel) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getFetchDirection()
	 */
	public int getFetchDirection() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getFetchSize()
	 */
	public int getFetchSize() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	public float getFloat(int columnIndex) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	public float getFloat(String columnLabel) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getInt(int)
	 */
	public int getInt(int columnIndex) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	public int getInt(String columnLabel) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getLong(int)
	 */
	public long getLong(int columnIndex) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	public long getLong(String columnLabel) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getMetaData()
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getNCharacterStream(int)
	 */
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
	 */
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return null;
	}

	

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getNString(int)
	 */
	public String getNString(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getNString(java.lang.String)
	 */
	public String getNString(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getObject(int)
	 */
	public Object getObject(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	public Object getObject(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getRef(int)
	 */
	public Ref getRef(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	public Ref getRef(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getRow()
	 */
	public int getRow() throws SQLException {
		return 0;
	}


	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getShort(int)
	 */
	public short getShort(int columnIndex) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	public short getShort(String columnLabel) throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getStatement()
	 */
	public Statement getStatement() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getString(int)
	 */
	public String getString(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	public String getString(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	public Time getTime(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	public Timestamp getTimestamp(String columnLabel, Calendar cal)
			throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getType()
	 */
	public int getType() throws SQLException {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getURL(int)
	 */
	public URL getURL(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	public URL getURL(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#insertRow()
	 */
	public void insertRow() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	public boolean isAfterLast() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	public boolean isBeforeFirst() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#isFirst()
	 */
	public boolean isFirst() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#isLast()
	 */
	public boolean isLast() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#last()
	 */
	public boolean last() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	public void moveToCurrentRow() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	public void moveToInsertRow() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#next()
	 */
	public boolean next() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#previous()
	 */
	public boolean previous() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#refreshRow()
	 */
	public void refreshRow() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#relative(int)
	 */
	public boolean relative(int rows) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#rowInserted()
	 */
	public boolean rowInserted() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#setFetchDirection(int)
	 */
	public void setFetchDirection(int direction) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#setFetchSize(int)
	 */
	public void setFetchSize(int rows) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
	 */
	public void updateArray(int columnIndex, Array x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
	 */
	public void updateArray(String columnLabel, Array x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)
	 */
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream)
	 */
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)
	 */
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, int)
	 */
	public void updateAsciiStream(String columnLabel, InputStream x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, long)
	 */
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, long)
	 */
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
	 */
	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	public void updateBigDecimal(String columnLabel, BigDecimal x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)
	 */
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream)
	 */
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)
	 */
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, int)
	 */
	public void updateBinaryStream(String columnLabel, InputStream x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, long)
	 */
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, long)
	 */
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
	 */
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
	 */
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)
	 */
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream)
	 */
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)
	 */
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream, long)
	 */
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBoolean(int, boolean)
	 */
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
	 */
	public void updateBoolean(String columnLabel, boolean x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateByte(int, byte)
	 */
	public void updateByte(int columnIndex, byte x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
	 */
	public void updateByte(String columnLabel, byte x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBytes(int, byte[])
	 */
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
	 */
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)
	 */
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader)
	 */
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)
	 */
	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, int)
	 */
	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, long)
	 */
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
	 */
	public void updateClob(int columnIndex, Clob x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
	 */
	public void updateClob(String columnLabel, Clob x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
	 */
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
	 */
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
	 */
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)
	 */
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
	 */
	public void updateDate(int columnIndex, Date x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
	 */
	public void updateDate(String columnLabel, Date x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	public void updateDouble(int columnIndex, double x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
	 */
	public void updateDouble(String columnLabel, double x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	public void updateFloat(int columnIndex, float x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
	 */
	public void updateFloat(String columnLabel, float x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	public void updateInt(int columnIndex, int x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
	 */
	public void updateInt(String columnLabel, int x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	public void updateLong(int columnIndex, long x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
	 */
	public void updateLong(String columnLabel, long x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
	 */
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader)
	 */
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)
	 */
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
	}


	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
	 */
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
	 */
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
	}

  /** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
	 */
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)
	 */
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
	 */
	public void updateNString(int columnIndex, String nString)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)
	 */
	public void updateNString(String columnLabel, String nString)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	public void updateNull(int columnIndex) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNull(java.lang.String)
	 */
	public void updateNull(String columnLabel) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
	 */
	public void updateObject(int columnIndex, Object x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
	 */
	public void updateObject(String columnLabel, Object x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
	 */
	public void updateObject(int columnIndex, Object x, int scaleOrLength)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)
	 */
	public void updateObject(String columnLabel, Object x, int scaleOrLength)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
	 */
	public void updateRef(int columnIndex, Ref x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
	 */
	public void updateRef(String columnLabel, Ref x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateRow()
	 */
	public void updateRow() throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
	 */
	// #ifdef JDK>6
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
	}

	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException {
	}
	public NClob getNClob(int columnIndex) throws SQLException {
		return null;
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		return null;
	}
	public RowId getRowId(int columnIndex) throws SQLException {
		return null;
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		return null;
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return null;
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return null;
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
	}
	// #endif JDK>6  

  // #ifdef JDK7
//  @Override
  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
    return null;
  }

//  @Override
  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
    return null;
  }
  // #endif JDK7

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateShort(int, short)
	 */
	public void updateShort(int columnIndex, short x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
	 */
	public void updateShort(String columnLabel, short x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateString(int, java.lang.String)
	 */
	public void updateString(int columnIndex, String x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)
	 */
	public void updateString(String columnLabel, String x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
	 */
	public void updateTime(int columnIndex, Time x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
	 */
	public void updateTime(String columnLabel, Time x) throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
	 */
	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	public void updateTimestamp(String columnLabel, Timestamp x)
			throws SQLException {
	}

	/** {@inheritDoc}
	 * @see java.sql.ResultSet#wasNull()
	 */
	public boolean wasNull() throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/** {@inheritDoc}
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

}
