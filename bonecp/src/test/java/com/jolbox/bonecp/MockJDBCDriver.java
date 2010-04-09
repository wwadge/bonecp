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
	/** Instance. */
	private static MockJDBCDriver driverInstance;

	/**
	 * Default constructor
	 * @throws SQLException 
	 */
	public MockJDBCDriver() throws SQLException{
		// default constructor
		DriverManager.registerDriver(this);
		driverInstance = this;
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
		return true; // accept anything
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	// @Override
	public Connection connect(String url, Properties info) throws SQLException {
		if (this.connection != null){
			return this.connection;
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
	public static void disable() throws SQLException{
		DriverManager.deregisterDriver(driverInstance);
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
}
