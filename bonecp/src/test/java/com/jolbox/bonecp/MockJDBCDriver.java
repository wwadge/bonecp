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
	private Connection connection;
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
	
	/** Connection handle to return
	 * @param connection
	 * @throws SQLException 
	 */
	public MockJDBCDriver(MockJDBCAnswer mockJDBCAnswer) throws SQLException{
		this();
		this.mockJDBCAnswer = mockJDBCAnswer;
	}
	/** {@inheritDoc}
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return true; // accept anything
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		return this.mockJDBCAnswer.answer();
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#getMajorVersion()
	 */
	@Override
	public int getMajorVersion() {
		return 1;
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#getMinorVersion()
	 */
	@Override
	public int getMinorVersion() {
		return 0;
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
	 */
	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return new DriverPropertyInfo[0];
	}

	/** {@inheritDoc}
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	@Override
	public boolean jdbcCompliant() {
		return true;
	}
	
}
