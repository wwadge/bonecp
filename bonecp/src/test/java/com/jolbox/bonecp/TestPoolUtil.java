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

package com.jolbox.bonecp;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/** Tests util routines.
 * @author wallacew
 *
 */
public class TestPoolUtil {

	/**
	 * Tests formatting stuff.
	 */
	@Test
	public void testPoolUtil(){
			Map<Object, Object> logParams = new LinkedHashMap<Object, Object>();
		
			logParams.put("1", "123");
			logParams.put("2", "456");

			
			// test proper replacement/escaping
			assertEquals("ID=123 AND FOO='?' and LALA=\"BOO\" 456", PoolUtil.fillLogParams("ID=? AND FOO='?' and LALA=\"BOO\" ?", logParams));
		}
	

	/**
	 */
	@Test
	public void testPoolUtilCodeCoverage() {
		Map<Object, Object> logParams = new LinkedHashMap<Object, Object>();
		logParams.put("1", null);
		logParams.put("2", new Clob() {
			
			public void truncate(long len) throws SQLException {
				// do nothing
			}
			
			public int setString(long pos, String str, int offset, int len)
					throws SQLException {
				return 0;
			}
			
			public int setString(long pos, String str) throws SQLException {
				return 0;
			}
			
			public Writer setCharacterStream(long pos) throws SQLException {
				return null;
			}
			
			public OutputStream setAsciiStream(long pos) throws SQLException {
				return null;
			}
			
			public long position(Clob searchstr, long start) throws SQLException {
				return 0;
			}
			
			public long position(String searchstr, long start) throws SQLException {
				return 0;
			}
			
			public long length() throws SQLException {
				return 0;
			}
			
			public String getSubString(long pos, int length) throws SQLException {
				return null;
			}
			
			public Reader getCharacterStream(long pos, long length) throws SQLException {
				return null;
			}
			
			public Reader getCharacterStream() throws SQLException {
				return null;
			}
			
			public InputStream getAsciiStream() throws SQLException {
				return null;
			}
			
			public void free() throws SQLException {
				// do nothing
			}
		});
		logParams.put("5", new Ref() {
			
			public void setObject(Object value) throws SQLException {
				// do nothing
			}
			
			public Object getObject(Map<String, Class<?>> map) throws SQLException {
				return null;
			}
			
			public Object getObject() throws SQLException {
				return null;
			}
			
			public String getBaseTypeName() throws SQLException {
				return "ref";
			}
		});
		
		logParams.put("4", new Array() {
			
			public ResultSet getResultSet(long index, int count,
					Map<String, Class<?>> map) throws SQLException {
				return null;
			}
			
			public ResultSet getResultSet(long index, int count) throws SQLException {
				return null;
			}
			
			public ResultSet getResultSet(Map<String, Class<?>> map)
					throws SQLException {
				return null;
			}
			
			public ResultSet getResultSet() throws SQLException {
				return null;
			}
			
			public String getBaseTypeName() throws SQLException {
				return "foo";
			}
			
			public int getBaseType() throws SQLException {
				return 0;
			}
			
			public Object getArray(long index, int count, Map<String, Class<?>> map)
					throws SQLException {
				return null;
			}
			
			public Object getArray(long index, int count) throws SQLException {
				return null;
			}
			
			public Object getArray(Map<String, Class<?>> map) throws SQLException {
				return null;
			}
			
			public Object getArray() throws SQLException {
				return null;
			}
			
			public void free() throws SQLException {
				// do nothing
			}
		});
		
		logParams.put("3", new Blob() {
			
			public void truncate(long len) throws SQLException {
				// do nothing
			}
			
			public int setBytes(long pos, byte[] bytes, int offset, int len)
					throws SQLException {
				return 0;
			}
			
			public int setBytes(long pos, byte[] bytes) throws SQLException {
				return 0;
			}
			
			public OutputStream setBinaryStream(long pos) throws SQLException {
				return null;
			}
			
			public long position(Blob pattern, long start) throws SQLException {
				return 0;
			}
			
			public long position(byte[] pattern, long start) throws SQLException {
				return 0;
			}
			
			public long length() throws SQLException {
				return 0;
			}
			
			public byte[] getBytes(long pos, int length) throws SQLException {
				return null;
			}
			
			public InputStream getBinaryStream(long pos, long length)
					throws SQLException {
				return null;
			}
			
			public InputStream getBinaryStream() throws SQLException {
				return null;
			}
			
			public void free() throws SQLException {
				// do nothing
			}
		});

		// more code coverage
		assertEquals("NULL (cblob of length 0) (ref of type3) (array of type3) (blob of length 0)", PoolUtil.fillLogParams("? ? ? ? ?", logParams));
	}
	
	/**
	 */
	@Test
	public void testPoolUtilCodeCoverageErrorCases() {
		Map<Object, Object> logParams = new LinkedHashMap<Object, Object>();
		logParams.put("1", null);
		logParams.put("2", new Clob() {
			
			public void truncate(long len) throws SQLException {
				// do nothing
			}
			
			public int setString(long pos, String str, int offset, int len)
					throws SQLException {
				return 0;
			}
			
			public int setString(long pos, String str) throws SQLException {
				return 0;
			}
			
			public Writer setCharacterStream(long pos) throws SQLException {
				return null;
			}
			
			public OutputStream setAsciiStream(long pos) throws SQLException {
				return null;
			}
			
			public long position(Clob searchstr, long start) throws SQLException {
				return 0;
			}
			
			public long position(String searchstr, long start) throws SQLException {
				return 0;
			}
			
			public long length() throws SQLException {
				throw new SQLException();
			}
			
			public String getSubString(long pos, int length) throws SQLException {
				return null;
			}
			
			public Reader getCharacterStream(long pos, long length) throws SQLException {
				return null;
			}
			
			public Reader getCharacterStream() throws SQLException {
				return null;
			}
			
			public InputStream getAsciiStream() throws SQLException {
				return null;
			}
			
			public void free() throws SQLException {
				// do nothing
			}
		});
		logParams.put("5", new Ref() {
			
			public void setObject(Object value) throws SQLException {
				// do nothing
			}
			
			public Object getObject(Map<String, Class<?>> map) throws SQLException {
				return null;
			}
			
			public Object getObject() throws SQLException {
				return null;
			}
			
			public String getBaseTypeName() throws SQLException {
				throw new SQLException();
			}
		});
		
		logParams.put("4", new Array() {
			
			public ResultSet getResultSet(long index, int count,
					Map<String, Class<?>> map) throws SQLException {
				return null;
			}
			
			public ResultSet getResultSet(long index, int count) throws SQLException {
				return null;
			}
			
			public ResultSet getResultSet(Map<String, Class<?>> map)
					throws SQLException {
				return null;
			}
			
			public ResultSet getResultSet() throws SQLException {
				return null;
			}
			
			public String getBaseTypeName() throws SQLException {
				throw new SQLException();
			}
			
			public int getBaseType() throws SQLException {
				throw new SQLException();
			}
			
			public Object getArray(long index, int count, Map<String, Class<?>> map)
					throws SQLException {
				return null;
			}
			
			public Object getArray(long index, int count) throws SQLException {
				return null;
			}
			
			public Object getArray(Map<String, Class<?>> map) throws SQLException {
				return null;
			}
			
			public Object getArray() throws SQLException {
				return null;
			}
			
			public void free() throws SQLException {
				// do nothing
			}
		});
		
		logParams.put("3", new Blob() {
			
			public void truncate(long len) throws SQLException {
				// do nothing
			}
			
			public int setBytes(long pos, byte[] bytes, int offset, int len)
					throws SQLException {
				return 0;
			}
			
			public int setBytes(long pos, byte[] bytes) throws SQLException {
				return 0;
			}
			
			public OutputStream setBinaryStream(long pos) throws SQLException {
				return null;
			}
			
			public long position(Blob pattern, long start) throws SQLException {
				return 0;
			}
			
			public long position(byte[] pattern, long start) throws SQLException {
				return 0;
			}
			
			public long length() throws SQLException {
				throw new SQLException();
			}
			
			public byte[] getBytes(long pos, int length) throws SQLException {
				return null;
			}
			
			public InputStream getBinaryStream(long pos, long length)
					throws SQLException {
				return null;
			}
			
			public InputStream getBinaryStream() throws SQLException {
				return null;
			}
			
			public void free() throws SQLException {
				// do nothing
			}
		});

		// more code coverage
		assertEquals("NULL (cblob of unknown length) (ref of unknown type) (array of unknown type) (blob of unknown length) ?", PoolUtil.fillLogParams("? ? ? ? ? ?", logParams));
	}
	
	/**
	 * Test safePrint method.
	 */
	@Test
	public void testSafePrint(){
		new PoolUtil(); // code coverage only.
		String s = "test";
		assertEquals("nulltest", PoolUtil.safePrint(null, s));
	}
}
