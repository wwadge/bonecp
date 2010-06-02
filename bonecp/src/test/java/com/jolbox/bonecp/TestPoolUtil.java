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

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialRef;

import org.hsqldb.jdbc.jdbcBlob;
import org.hsqldb.jdbc.jdbcClob;
import org.junit.Test;

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
			Map<Object, Object> logParams = new LinkedHashMap<Object, Object>();
		
			logParams.put("1", "123");
			logParams.put("2", "456");
			logParams.put("3", new jdbcBlob("Hello".getBytes()));
			logParams.put("4", new jdbcClob("Hello"));
			logParams.put("5", new SerialArray(new Array() {
				
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
					return "Base type name";
				}
				
				public int getBaseType() throws SQLException {
					return 0;
				}
				
				public Object getArray(long index, int count, Map<String, Class<?>> map)
						throws SQLException {
					return new Object[]{};
				}
				
				public Object getArray(long index, int count) throws SQLException {
					return new Object[]{};
				}
				
				public Object getArray(Map<String, Class<?>> map) throws SQLException {
					return null;
				}
				
				public Object getArray() throws SQLException {
					return new Object[]{};
				}
				@SuppressWarnings("unused")
				public void free() throws SQLException {
					// do nothing
				}
			}));
			logParams.put("6", new SerialRef(new Ref() {
				
				public void setObject(Object value) throws SQLException {
					// do nothing
				}
				
				public Object getObject(Map<String, Class<?>> map) throws SQLException {
					return new Object();
				}
				
				public Object getObject() throws SQLException {
					return new Object();
				}
				
				public String getBaseTypeName() throws SQLException {
					return "base type name";
				}
			}));
			
			// test proper replacement/escaping
			assertEquals("ID=123 AND FOO='?' and LALA=\"BOO\" 456 (blob of length 5) (cblob of length 5) (array of type14) (ref of type14) ?", PoolUtil.fillLogParams("ID=? AND FOO='?' and LALA=\"BOO\" ? ? ? ? ? ?", logParams));
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
			assertEquals("ID=123 AND FOO='?' and LALA=\"BOO\" NULL (blob of unknown length) (cblob of unknown length) (array of unknown type) (ref of unknown type) ?", PoolUtil.fillLogParams("ID=? AND FOO='?' and LALA=\"BOO\" ? ? ? ? ? ?", logParams));
		}
	
	/**
	 * Tests safePrint method
	 */
	@Test
	public void testPoolUtilSafePrint() {
		assertEquals("nullhello", PoolUtil.safePrint(null, "hello"));
		new PoolUtil(); //just for coverage
	}
}
