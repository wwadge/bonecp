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

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataSource for use with LazyConnection Provider etc.
 *
 * @author wallacew
 */
public class BoneCPDataSource extends BoneCPConfig implements DataSource, ObjectFactory {
	/** Serialization UID. */
	private static final long serialVersionUID = -1561804548443209469L;
	/** Config setting. */
	private PrintWriter logWriter = null;
	/** Pool handle. */
	private transient volatile BoneCP pool = null;
	/** Lock for init. */
	private ReadWriteLock rwl = new ReentrantReadWriteLock();
	/** JDBC driver to use. */
	private String driverClass;
	/** Class logger. */
	private static final Logger logger = LoggerFactory.getLogger(BoneCPDataSource.class);
	/**
	 * Default empty constructor.
	 *
	 */
	public BoneCPDataSource() {
		// default constructor
	}
	/**
	 * 
	 *
	 * @param config
	 */
	public BoneCPDataSource(BoneCPConfig config) {
		Field[] fields = BoneCPConfig.class.getDeclaredFields();
		for (Field field: fields){
			try {
				field.setAccessible(true);
				field.set(this, field.get(config));
			} catch (Exception e) {
				// should never happen
			}
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		if (this.pool == null){
			maybeInit();
		}
		return this.pool.getConnection();
	}

	/**
	 * Close the datasource. 
	 *
	 */
	public void close(){
		if (this.pool != null){
			this.pool.shutdown();
		}
	}

	/**
	 * @throws SQLException 
	 * 
	 *
	 */
	private void maybeInit() throws SQLException {
		this.rwl.readLock().lock();
		if (this.pool == null){
			this.rwl.readLock().unlock();
			this.rwl.writeLock().lock();
			if (this.pool == null){ //read might have passed, write might not
				try {
					if (this.getDriverClass() != null){
						loadClass(this.getDriverClass());
					}
				}
				catch (ClassNotFoundException e) {
					throw new SQLException(PoolUtil.stringifyException(e));
				}


				logger.debug(this.toString());

				this.pool = new BoneCP(this);
			}

			this.rwl.writeLock().unlock(); // Unlock write
		} else {
			this.rwl.readLock().unlock(); // Unlock read
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection(String username, String password)
	throws SQLException {
		throw new UnsupportedOperationException("getConnectionString username, String password) is not supported");
	}

	/**
	 * Retrieves the log writer for this DataSource object.
	 * 
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return this.logWriter;
	}

	/**
	 * Gets the maximum time in seconds that this data source can wait while attempting to connect to a database. 
	 * A value of zero means that the timeout is the default system timeout if there is one; otherwise, it means that there is no timeout. When a DataSource object is created, the login timeout is initially zero.
	 * 
	 */
	public int getLoginTimeout()
	throws SQLException {
		throw new UnsupportedOperationException("getLoginTimeout is unsupported.");
	}

	/**
	 * Sets the log writer for this DataSource object to the given java.io.PrintWriter object.
	 */
	public void setLogWriter(PrintWriter out)
	throws SQLException {
		this.logWriter = out;
	}

	/**
	 * Sets the maximum time in seconds that this data source will wait while 
	 * attempting to connect to a database. A value of zero specifies that the timeout is the default 
	 * system timeout if there is one; otherwise, it specifies that there is no timeout. When a DataSource object is created, the login timeout is initially zero.
	 */
	public void setLoginTimeout(int seconds)
	throws SQLException {
		throw new UnsupportedOperationException("setLoginTimeout is unsupported.");
	}

	/**
	 * Returns true if this either implements the interface argument or is directly or indirectly a wrapper for an object that does.
	 * @param arg0 class
	 * @return t/f
	 * @throws SQLException on error
	 *
	 */
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	/**
	 * Returns an object that implements the given interface to allow access to non-standard methods, 
	 * or standard methods not exposed by the proxy.
	 * @param arg0 obj
	 * @return unwrapped object
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	public Object unwrap(Class arg0) throws SQLException {
		return null;
	}


	/**
	 * Gets driver class set in config. 
	 *
	 * @return Driver class set in config
	 */
	public String getDriverClass() {
		return this.driverClass;
	}


	/**
	 * Sets driver to use (called via reflection).
	 *
	 * @param driverClass Driver to use
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}


	/**
	 * Returns the total leased connections.
	 *
	 * @return total leased connections
	 */
	public int getTotalLeased() {
		return this.pool.getTotalLeased();
	}

	/** Returns a configuration object built during initialization of the connection pool. 
	 *   
	 * @return the config
	 */
	public BoneCPConfig getConfig() {
		return this;
	}



	/* (non-Javadoc)
	 * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	@Override
	public Object getObjectInstance(Object object, Name name, Context context, Hashtable<?, ?> table) throws Exception {

		Reference ref = (Reference) object;
		Enumeration<RefAddr> addrs = ref.getAll();
		Properties props = new Properties();
		while (addrs.hasMoreElements()) {
			RefAddr addr = addrs.nextElement();
			if (addr.getType().equals("driverClassName")){
				Class.forName((String) addr.getContent());
			} else {
				props.put(addr.getType(), addr.getContent());
			}
		}		
		BoneCPConfig config = new BoneCPConfig(props);

		return new BoneCPDataSource(config);
}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}