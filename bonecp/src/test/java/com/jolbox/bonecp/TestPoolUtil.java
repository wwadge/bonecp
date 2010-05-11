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

import java.sql.Array;
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
			assertEquals("ID=123 AND FOO='?' and LALA=\"BOO\" 456 ? ? ?", PoolUtil.fillLogParams("ID=? AND FOO='?' and LALA=\"BOO\" ?", logParams));
		}
}
