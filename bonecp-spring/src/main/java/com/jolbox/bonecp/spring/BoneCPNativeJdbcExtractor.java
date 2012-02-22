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

package com.jolbox.bonecp.spring;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter;
import org.springframework.util.ReflectionUtils;

/**
 * Implementation of the {@link NativeJdbcExtractor} interface for BoneCP.
 *
 * <p>Returns the underlying native Connection and Statement to application code instead of BoneCP logical handles.
 * The returned JDBC classes can then safely be cast, e.g. to <code>oracle.jdbc.OracleConnection</code>.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with
 * a BoneCP connection pool: If a given object is not a BoneCP handle, it will be returned as-is.
 *
 * @see com.jolbox.bonecp.ConnectionHandle#getInternalConnection()
 * @see com.jolbox.bonecp.StatementHandle#getInternalStatement()
 * 
 * @author eric239
 */
public class BoneCPNativeJdbcExtractor extends NativeJdbcExtractorAdapter {
	/** Connection handle. */
	private final Class<?> connectionHandleClass;
	/** Statement handle. */
	private final Class<?> statementHandleClass;
	/** Internal connection method handle. */
	private final Method getInternalConnectionMethod;
	/** Internal statement method handle. */
	private final Method getInternalStatementMethod;
	
	/**
	 * Default constructor.
	 */
	public BoneCPNativeJdbcExtractor() {
		final ClassLoader cl = getClass().getClassLoader();
		try {
			this.connectionHandleClass = cl.loadClass("com.jolbox.bonecp.ConnectionHandle");
			this.statementHandleClass = cl.loadClass("com.jolbox.bonecp.StatementHandle");
			this.getInternalConnectionMethod = this.connectionHandleClass.getMethod("getInternalConnection");
			this.getInternalStatementMethod = this.statementHandleClass.getMethod("getInternalStatement");
		} catch (Exception e) {
			throw new IllegalStateException("Failed to initialize because BoneCP API classes are not available", e);
		}
	}

	
	/**
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter#doGetNativeConnection(java.sql.Connection)
	 */
	@Override
	protected Connection doGetNativeConnection(Connection conn) throws SQLException {
		if (this.connectionHandleClass.isAssignableFrom(conn.getClass())) {
			return (Connection) ReflectionUtils.invokeJdbcMethod(this.getInternalConnectionMethod, conn);
		}
		return conn;
	}


	/**
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter#getNativeStatement(java.sql.Statement)
	 */
	@Override
	public Statement getNativeStatement(Statement stmt) throws SQLException {
		if (this.statementHandleClass.isAssignableFrom(stmt.getClass())) {
			return (Statement) ReflectionUtils.invokeJdbcMethod(this.getInternalStatementMethod, stmt);
		}
		return stmt;
	}

	/**
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter#getNativePreparedStatement(java.sql.PreparedStatement)
	 */
	@Override
	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		return (PreparedStatement) getNativeStatement(ps);
	}

	/**
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter#getNativeCallableStatement(java.sql.CallableStatement)
	 */
	@Override
	public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
		return (CallableStatement) getNativeStatement(cs);
	}

}
