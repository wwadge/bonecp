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
package java.sql;

import java.sql.SQLException;
import java.util.Map;

/**
 * @author wwadge/eclipse
 *
 */
public interface CallableStatement extends java.sql.PreparedStatement {
		   void registerOutParameter(int arg0, int arg1) throws java.sql.SQLException;
		   void registerOutParameter(int arg0, int arg1, int arg2) throws java.sql.SQLException;
		   boolean wasNull() throws java.sql.SQLException;
		   String getString(int arg0) throws java.sql.SQLException;
		   boolean getBoolean(int arg0) throws java.sql.SQLException;
		   byte getByte(int arg0) throws java.sql.SQLException;
		   short getShort(int arg0) throws java.sql.SQLException;
		   int getInt(int arg0) throws java.sql.SQLException;
		   long getLong(int arg0) throws java.sql.SQLException;
		   float getFloat(int arg0) throws java.sql.SQLException;
		   double getDouble(int arg0) throws java.sql.SQLException;
		   java.math.BigDecimal getBigDecimal(int arg0, int arg1) throws java.sql.SQLException;
		   byte[] getBytes(int arg0) throws java.sql.SQLException;
		   java.sql.Date getDate(int arg0) throws java.sql.SQLException;
		   java.sql.Time getTime(int arg0) throws java.sql.SQLException;
		   java.sql.Timestamp getTimestamp(int arg0) throws java.sql.SQLException;
		   Object getObject(int arg0) throws java.sql.SQLException;
		   Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException;
		   java.math.BigDecimal getBigDecimal(int arg0) throws java.sql.SQLException;
		   java.sql.Ref getRef(int arg0) throws java.sql.SQLException;
		   java.sql.Blob getBlob(int arg0) throws java.sql.SQLException;
		   java.sql.Clob getClob(int arg0) throws java.sql.SQLException;
		   java.sql.Array getArray(int arg0) throws java.sql.SQLException;
		   java.sql.Date getDate(int arg0, java.util.Calendar arg1) throws java.sql.SQLException;
		   java.sql.Time getTime(int arg0, java.util.Calendar arg1) throws java.sql.SQLException;
		   java.sql.Timestamp getTimestamp(int arg0, java.util.Calendar arg1) throws java.sql.SQLException;
		   void registerOutParameter(int arg0, int arg1, String arg2) throws java.sql.SQLException;
		   void registerOutParameter(String arg0, int arg1) throws java.sql.SQLException;
		   void registerOutParameter(String arg0, int arg1, int arg2) throws java.sql.SQLException;
		   void registerOutParameter(String arg0, int arg1, String arg2) throws java.sql.SQLException;
		   java.net.URL getURL(int arg0) throws java.sql.SQLException;
		   void setURL(String arg0, java.net.URL arg1) throws java.sql.SQLException;
		   void setNull(String arg0, int arg1) throws java.sql.SQLException;
		   void setBoolean(String arg0, boolean arg1) throws java.sql.SQLException;
		   void setByte(String arg0, byte arg1) throws java.sql.SQLException;
		   void setShort(String arg0, short arg1) throws java.sql.SQLException;
		   void setInt(String arg0, int arg1) throws java.sql.SQLException;
		   void setLong(String arg0, long arg1) throws java.sql.SQLException;
		   void setFloat(String arg0, float arg1) throws java.sql.SQLException;
		   void setDouble(String arg0, double arg1) throws java.sql.SQLException;
		   void setBigDecimal(String arg0, java.math.BigDecimal arg1) throws java.sql.SQLException;
		   void setString(String arg0, String arg1) throws java.sql.SQLException;
		   void setBytes(String arg0, byte[] arg1) throws java.sql.SQLException;
		   void setDate(String arg0, java.sql.Date arg1) throws java.sql.SQLException;
		   void setTime(String arg0, java.sql.Time arg1) throws java.sql.SQLException;
		   void setTimestamp(String arg0, java.sql.Timestamp arg1) throws java.sql.SQLException;
		   void setAsciiStream(String arg0, java.io.InputStream arg1, int arg2) throws java.sql.SQLException;
		   void setBinaryStream(String arg0, java.io.InputStream arg1, int arg2) throws java.sql.SQLException;
		   void setObject(String arg0, Object arg1, int arg2, int arg3) throws java.sql.SQLException;
		   void setObject(String arg0, Object arg1, int arg2) throws java.sql.SQLException;
		   void setObject(String arg0, Object arg1) throws java.sql.SQLException;
		   void setCharacterStream(String arg0, java.io.Reader arg1, int arg2) throws java.sql.SQLException;
		   void setDate(String arg0, java.sql.Date arg1, java.util.Calendar arg2) throws java.sql.SQLException;
		   void setTime(String arg0, java.sql.Time arg1, java.util.Calendar arg2) throws java.sql.SQLException;
		   void setTimestamp(String arg0, java.sql.Timestamp arg1, java.util.Calendar arg2) throws java.sql.SQLException;
		   void setNull(String arg0, int arg1, String arg2) throws java.sql.SQLException;
		   String getString(String arg0) throws java.sql.SQLException;
		   boolean getBoolean(String arg0) throws java.sql.SQLException;
		   byte getByte(String arg0) throws java.sql.SQLException;
		   short getShort(String arg0) throws java.sql.SQLException;
		   int getInt(String arg0) throws java.sql.SQLException;
		   long getLong(String arg0) throws java.sql.SQLException;
		   float getFloat(String arg0) throws java.sql.SQLException;
		   double getDouble(String arg0) throws java.sql.SQLException;
		   byte[] getBytes(String arg0) throws java.sql.SQLException;
		   java.sql.Date getDate(String arg0) throws java.sql.SQLException;
		   java.sql.Time getTime(String arg0) throws java.sql.SQLException;
		   java.sql.Timestamp getTimestamp(String arg0) throws java.sql.SQLException;
		   Object getObject(String arg0) throws java.sql.SQLException;
		   java.math.BigDecimal getBigDecimal(String arg0) throws java.sql.SQLException;
		   Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException;
		   java.sql.Ref getRef(String arg0) throws java.sql.SQLException;
		   java.sql.Blob getBlob(String arg0) throws java.sql.SQLException;
		   java.sql.Clob getClob(String arg0) throws java.sql.SQLException;
		   java.sql.Array getArray(String arg0) throws java.sql.SQLException;
		   java.sql.Date getDate(String arg0, java.util.Calendar arg1) throws java.sql.SQLException;
		   java.sql.Time getTime(String arg0, java.util.Calendar arg1) throws java.sql.SQLException;
		   java.sql.Timestamp getTimestamp(String arg0, java.util.Calendar arg1) throws java.sql.SQLException;
		   java.net.URL getURL(String arg0) throws java.sql.SQLException;
		   java.sql.RowId getRowId(int arg0) throws java.sql.SQLException;
		   java.sql.RowId getRowId(String arg0) throws java.sql.SQLException;
		   void setRowId(String arg0, java.sql.RowId arg1) throws java.sql.SQLException;
		   void setNString(String arg0, String arg1) throws java.sql.SQLException;
		   void setNCharacterStream(String arg0, java.io.Reader arg1, long arg2) throws java.sql.SQLException;
		   void setNClob(String arg0, java.sql.NClob arg1) throws java.sql.SQLException;
		   void setClob(String arg0, java.io.Reader arg1, long arg2) throws java.sql.SQLException;
		   void setBlob(String arg0, java.io.InputStream arg1, long arg2) throws java.sql.SQLException;
		   void setNClob(String arg0, java.io.Reader arg1, long arg2) throws java.sql.SQLException;
		   java.sql.NClob getNClob(int arg0) throws java.sql.SQLException;
		   java.sql.NClob getNClob(String arg0) throws java.sql.SQLException;
		   void setSQLXML(String arg0, java.sql.SQLXML arg1) throws java.sql.SQLException;
		   java.sql.SQLXML getSQLXML(int arg0) throws java.sql.SQLException;
		   java.sql.SQLXML getSQLXML(String arg0) throws java.sql.SQLException;
		   String getNString(int arg0) throws java.sql.SQLException;
		   String getNString(String arg0) throws java.sql.SQLException;
		   java.io.Reader getNCharacterStream(int arg0) throws java.sql.SQLException;
		   java.io.Reader getNCharacterStream(String arg0) throws java.sql.SQLException;
		   java.io.Reader getCharacterStream(int arg0) throws java.sql.SQLException;
		   java.io.Reader getCharacterStream(String arg0) throws java.sql.SQLException;
		   void setBlob(String arg0, java.sql.Blob arg1) throws java.sql.SQLException;
		   void setClob(String arg0, java.sql.Clob arg1) throws java.sql.SQLException;
		   void setAsciiStream(String arg0, java.io.InputStream arg1, long arg2) throws java.sql.SQLException;
		   void setBinaryStream(String arg0, java.io.InputStream arg1, long arg2) throws java.sql.SQLException;
		   void setCharacterStream(String arg0, java.io.Reader arg1, long arg2) throws java.sql.SQLException;
		   void setAsciiStream(String arg0, java.io.InputStream arg1) throws java.sql.SQLException;
		   void setBinaryStream(String arg0, java.io.InputStream arg1) throws java.sql.SQLException;
		   void setCharacterStream(String arg0, java.io.Reader arg1) throws java.sql.SQLException;
		   void setNCharacterStream(String arg0, java.io.Reader arg1) throws java.sql.SQLException;
		   void setClob(String arg0, java.io.Reader arg1) throws java.sql.SQLException;
		   void setBlob(String arg0, java.io.InputStream arg1) throws java.sql.SQLException;
		   void setNClob(String arg0, java.io.Reader arg1) throws java.sql.SQLException;
	
	
	 <T> T getObject(int parameterIndex, Class<T> type) throws SQLException;
	 <T> T getObject(String parameterName, Class<T> type) throws SQLException;

}
