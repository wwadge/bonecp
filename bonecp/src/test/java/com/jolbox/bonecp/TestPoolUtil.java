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

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.*;

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

import com.google.common.collect.Maps;

/** Tests util routines.
 * @author wallacew
 *
 */
public class TestPoolUtil {
	/**
	 * Tests formatting stuff.
	 * @throws SQLException 
	 */
	@Test
	public void testPoolUtil() throws SQLException{
		Maps.newHashMap();
		
			Map<Object, Object> logParams = new LinkedHashMap<Object, Object>();
		
			logParams.put("1", "123");
			logParams.put("2", "456");
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
					return 5;
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
					//do nothing
				}
			});
			logParams.put("4", new Clob() {
				
				public void truncate(long len) throws SQLException {
					//do nothing
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
					return 5;
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
					//do nothing
				}
			});
			logParams.put("5", new Array() {
				
				public ResultSet getResultSet(long index, int count,
						Map<String, Class<?>> map) throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public ResultSet getResultSet(long index, int count) throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public ResultSet getResultSet(Map<String, Class<?>> map)
						throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public ResultSet getResultSet() throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public String getBaseTypeName() throws SQLException {
					return "Base type name";
				}
				
				public int getBaseType() throws SQLException {
					// TODO Auto-generated method stub
					return 14;
				}
				
				public Object getArray(long index, int count, Map<String, Class<?>> map)
						throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public Object getArray(long index, int count) throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public Object getArray(Map<String, Class<?>> map) throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public Object getArray() throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				public void free() throws SQLException {
					// TODO Auto-generated method stub
					
				}
			});
				
			
			logParams.put("6", new Ref() {
				
				public void setObject(Object value) throws SQLException {
					// TODO Auto-generated method stub
					
				}
				
				public Object getObject(Map<String, Class<?>> map) throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				
				public Object getObject() throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
				
				public String getBaseTypeName() throws SQLException {
					// TODO Auto-generated method stub
					return "type";
				}
			});
			logParams.put("7", new Integer(999));
				
			
			// test proper replacement/escaping
			assertEquals("ID='123' AND FOO='?' and LALA=\"BOO\" '456' (blob of length 5) (cblob of length 5) (array of type14) (ref of type4) 999 ?", PoolUtil.fillLogParams("ID=? AND FOO='?' and LALA=\"BOO\" ? ? ? ? ? ? ?", logParams));
		}
	
	/**
	 * Tests formatting stuff (exceptions case)
	 * @throws SQLException 
	 */
	@Test
	public void testPoolUtilExceptions() throws SQLException{
			Map<Object, Object> logParams = new LinkedHashMap<Object, Object>();
		
			logParams.put("1", "123");
			logParams.put("2", null);
			
			Blob mockBlob = createNiceMock(Blob.class);
			expect(mockBlob.length()).andThrow(new SQLException());
			replay(mockBlob);

			
			logParams.put("3", mockBlob);
			
			Clob mockClob = createNiceMock(Clob.class);
			expect(mockClob.length()).andThrow(new SQLException());
			replay(mockClob);

			
			logParams.put("4", mockClob);

			Array mockArray = createNiceMock(java.sql.Array.class);
			expect(mockArray.getBaseTypeName()).andThrow(new SQLException());
			replay(mockArray);

			logParams.put("5", mockArray);
			
			Ref mockSerialRef = createNiceMock(Ref.class);
			expect(mockSerialRef.getBaseTypeName()).andThrow(new SQLException());
			replay(mockSerialRef);
			logParams.put("6", mockSerialRef);
			
			// test proper replacement/escaping
			assertEquals("ID='123' AND FOO='?' and LALA=\"BOO\" NULL (blob of unknown length) (cblob of unknown length) (array of unknown type) (ref of unknown type) ?", PoolUtil.fillLogParams("ID=? AND FOO='?' and LALA=\"BOO\" ? ? ? ? ? ?", logParams));
		}
	
	/**
	 * Tests safePrint method
	 */
	@Test
	public void testPoolUtilSafePrint() {
		assertEquals("nullhello", PoolUtil.safePrint(null, "hello"));
		new PoolUtil(); //just for coverage
	}
	
	/**
	 * @throws SQLException
	 */
	@Test
	public void testPoolUtilNull() throws SQLException{
		PoolUtil.fillLogParams(null, null);
	}

}
