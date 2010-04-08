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

package com.jolbox.bonecp.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.util.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.hooks.ConnectionHook;


/**
 * Hibernate Connection Provider.
 *
 * @author wallacew
 */
public class BoneCPConnectionProvider implements ConnectionProvider {
	/** Config key. */
	protected static final String CONFIG_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_PASSWORD = "hibernate.connection.password";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_USERNAME = "hibernate.connection.username";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_URL = "hibernate.connection.url";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_DRIVER_CLASS_ALTERNATE = "javax.persistence.jdbc.driver";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_PASSWORD_ALTERNATE = "javax.persistence.jdbc.password";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_USERNAME_ALTERNATE = "javax.persistence.jdbc.user";
	/** Config key. */
	protected static final String CONFIG_CONNECTION_URL_ALTERNATE = "javax.persistence.jdbc.url";
	/** Connection pool handle. */
	private BoneCP pool;
	/** Isolation level. */
	private Integer isolation;
	/** Autocommit option. */
	private boolean autocommit;
	/** Classloader to use to load the jdbc driver. */
	private ClassLoader classLoader;
	/** Configuration handle. */
	private BoneCPConfig config;
	/** Class logger. */
	private static Logger logger = LoggerFactory.getLogger(BoneCPConnectionProvider.class);

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#close()
	 */
	public void close() throws HibernateException {
		if (this.pool != null){
			this.pool.shutdown();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#closeConnection(java.sql.Connection)
	 */
	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	} 

	/** Uppercases the first character.
	 * @param name
	 * @return the same string with the first letter in uppercase
	 */
	private static String upFirst(String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#configure(java.util.Properties)
	 */
	public void configure(Properties props) throws HibernateException {
		this.config = new BoneCPConfig();
		try {
			// Use reflection to read in all possible properties of int, String or boolean.
			for (Field field: BoneCPConfig.class.getDeclaredFields()){
				if (!Modifier.isFinal(field.getModifiers())){ // avoid logger etc.
					if (field.getType().equals(int.class)){
						Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), int.class);
						String val = props.getProperty("bonecp."+field.getName());
						if (val != null) {
							try{
								method.invoke(this.config, Integer.parseInt(val));
							} catch (NumberFormatException e){
								// do nothing, use the default value
							}
						}
					} if (field.getType().equals(long.class)){
						Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), long.class);
						String val = props.getProperty("bonecp."+field.getName());
						if (val != null) {
							try{
								method.invoke(this.config, Long.parseLong(val));
							} catch (NumberFormatException e){
								// do nothing, use the default value
							}
						}
					} else if (field.getType().equals(String.class)){
						Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), String.class);
						String val = props.getProperty("bonecp."+field.getName());
						if (val != null) {
							method.invoke(this.config, val);
						}
					} else if (field.getType().equals(boolean.class)){
						Method method = BoneCPConfig.class.getDeclaredMethod("set"+upFirst(field.getName()), boolean.class);
						String val = props.getProperty("bonecp."+field.getName());
						if (val != null) {
							method.invoke(this.config, Boolean.parseBoolean(val));
						}
					}
				}
			}

			// old hibernate config
			String url = props.getProperty(CONFIG_CONNECTION_URL);
			String username = props.getProperty(CONFIG_CONNECTION_USERNAME);
			String password = props.getProperty(CONFIG_CONNECTION_PASSWORD);
			String driver = props.getProperty(CONFIG_CONNECTION_DRIVER_CLASS);
			if (url == null){
				url = props.getProperty(CONFIG_CONNECTION_URL_ALTERNATE);
			}
			if (username == null){
				username = props.getProperty(CONFIG_CONNECTION_USERNAME_ALTERNATE);
			}
			if (password == null){
				password = props.getProperty(CONFIG_CONNECTION_PASSWORD_ALTERNATE);
			}
			if (driver == null){
				driver = props.getProperty(CONFIG_CONNECTION_DRIVER_CLASS_ALTERNATE);
			}

			if (url != null){
				this.config.setJdbcUrl(url);
			}
			if (username != null){
				this.config.setUsername(username);
			}
			if (password != null){
				this.config.setPassword(password);
			}


			// Remember Isolation level
			this.isolation = PropertiesHelper.getInteger(Environment.ISOLATION, props);
			this.autocommit = PropertiesHelper.getBoolean(Environment.AUTOCOMMIT, props);

			logger.debug(this.config.toString());

			if (driver != null && !driver.trim().equals("")){
				loadClass(driver);
			}
			if (this.config.getConnectionHookClassName() != null){
				Object hookClass = loadClass(this.config.getConnectionHookClassName()).newInstance();
				this.config.setConnectionHook((ConnectionHook) hookClass);
			}
			// create the connection pool
			this.pool = createPool(this.config);
		} catch (Exception e) {
			throw new HibernateException(e);
		} 
	}

	/** Loads the given class, respecting the given classloader.
	 * @param clazz class to load
	 * @return Loaded class
	 * @throws ClassNotFoundException
	 */
	protected Class<?> loadClass(String clazz) throws ClassNotFoundException {
		if (this.classLoader == null){
			return Class.forName(clazz);
		}

		return Class.forName(clazz, true, this.classLoader);

	}

	/** Creates the given connection pool with the given configuration. Extracted here to make unit mocking easier.
	 * @param config configuration object.
	 * @return BoneCP connection pool handle.
	 */
	protected BoneCP createPool(BoneCPConfig config) {
		try{ 
			return new BoneCP(config);
		}  catch (SQLException e) {
			throw new HibernateException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#getConnection()
	 */
	public Connection getConnection()
	throws SQLException {
		Connection connection = this.pool.getConnection();

		// set the Transaction Isolation if defined
		if (this.isolation !=null){
			connection.setTransactionIsolation( this.isolation.intValue() );
		}

		// toggle autoCommit to false if set
		if ( connection.getAutoCommit() != this.autocommit ){
			connection.setAutoCommit(this.autocommit);
		}
		return connection;


	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.hibernate.connection.ConnectionProvider#supportsAggressiveRelease()
	 */
	public boolean supportsAggressiveRelease() {
		return false;
	}

	/** Returns the configuration object being used.
	 * @return configuration object
	 */
	protected BoneCPConfig getConfig() {
		return this.config;
	}

	/** Returns the classloader to use when attempting to load the jdbc driver (if a value is given).
	 * @return the classLoader currently set.
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	/** Specifies the classloader to use when attempting to load the jdbc driver (if a value is given). Set to null to use the default
	 * loader.
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

}
