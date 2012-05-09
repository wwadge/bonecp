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
// #ifdef JDK7
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
// #endif JDK7

/** A Fake jdbc driver for mocking purposes.
 * @author Wallace
 *
 */
public class MockJDBCDriver implements Driver {
	/** Connection handle to return. */
	private volatile Connection connection = null;
	/** called to return. */
	private volatile MockJDBCAnswer mockJDBCAnswer;
	/** on/off setting. */
	private volatile static boolean active;

	/**
	 * Default constructor
	 * @throws SQLException 
	 */
	public MockJDBCDriver() throws SQLException{
		// default constructor
		DriverManager.registerDriver(this);
		this.active = true;
	}

	/** Stop intercepting requests.
	 * @throws SQLException
	 */
	public void unregister() throws SQLException{
		this.connection = null;
		this.mockJDBCAnswer = null;
		this.active = false;
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
	public synchronized boolean acceptsURL(String url) throws SQLException {
		return this.active && url.startsWith("jdbc:mock"); // accept anything
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	// @Override
	public synchronized Connection connect(String url, Properties info) throws SQLException {
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
	public synchronized DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
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

  // #ifdef JDK7
  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }
  // #endif JDK7

  /**
	 * Disable everything.
	 * @throws SQLException 
	 * @throws SQLException 
	 */
	public synchronized void disable() throws SQLException{
		this.connection = null;
		this.mockJDBCAnswer = null;
		DriverManager.deregisterDriver(this);
	}

	/**
	 * @return the connection
	 */
	public synchronized Connection getConnection() {
		return this.connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public synchronized  void setConnection(Connection connection) {
		this.connection = connection;
	}

	/** Return the jdbc answer class
	 * @return the mockJDBCAnswer
	 */
	public synchronized  MockJDBCAnswer getMockJDBCAnswer() {
		return this.mockJDBCAnswer;
	}

	/** Sets the jdbc mock answer.
	 * @param mockJDBCAnswer the mockJDBCAnswer to set
	 */
	public synchronized void setMockJDBCAnswer(MockJDBCAnswer mockJDBCAnswer) {
		this.mockJDBCAnswer = mockJDBCAnswer;
	}
}