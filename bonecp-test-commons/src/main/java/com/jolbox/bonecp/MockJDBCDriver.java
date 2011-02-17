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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/** A Fake jdbc driver for mocking purposes.
 * @author Wallace
 *
 */
public class MockJDBCDriver  implements Driver {
	/** Connection handle to return. */
	private Connection connection = null;
	/** called to return. */
	private MockJDBCAnswer mockJDBCAnswer;

	/**
	 * Default constructor
	 * @throws SQLException 
	 */
	public MockJDBCDriver() throws SQLException{
		// default constructor
		DriverManager.registerDriver(this);
	}

	public void unregister() throws SQLException{
		DriverManager.deregisterDriver(this);
	}

	/** Connection handle to return
	 * @param mockJDBCAnswer answer class
	 * @throws SQLException 
	 */
	public MockJDBCDriver(MockJDBCAnswer mockJDBCAnswer) throws SQLException{
		this();
		this.mockJDBCAnswer = mockJDBCAnswer;
	}
	
	/** Return the connection when requested.
	 * @param connection
	 * @throws SQLException
	 */
	public MockJDBCDriver(Connection connection) throws SQLException{
		this();
		this.connection = connection;
	}
	/** {@inheritDoc}
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
//	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("jdbc:mock"); // accept anything
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	// @Override
	public Connection connect(String url, Properties info) throws SQLException {
		if (url.startsWith("invalid") || url.equals("")){
			throw new SQLException("Mock Driver rejecting invalid URL");
		}
		if (this.connection != null){
			return this.connection;
		}
		
		if (this.mockJDBCAnswer == null){
			return new MockConnection();
		}
		
		return this.mockJDBCAnswer.answer();
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#getMajorVersion()
	 */
	// @Override
	public int getMajorVersion() {
		return 1;
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#getMinorVersion()
	 */
	// @Override
	public int getMinorVersion() {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
	 */
	// @Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return new DriverPropertyInfo[0];
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	// @Override
	public boolean jdbcCompliant() {
		return true;
	}
	
	/** 
	 * Disable everything.
	 * @throws SQLException 
	 * @throws SQLException 
	 */
	public void disable() throws SQLException{
		DriverManager.deregisterDriver(this);
	}

	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/** Return the jdbc answer class
	 * @return the mockJDBCAnswer
	 */
	public MockJDBCAnswer getMockJDBCAnswer() {
		return this.mockJDBCAnswer;
	}

	/** Sets the jdbc mock answer.
	 * @param mockJDBCAnswer the mockJDBCAnswer to set
	 */
	public void setMockJDBCAnswer(MockJDBCAnswer mockJDBCAnswer) {
		this.mockJDBCAnswer = mockJDBCAnswer;
	}
}