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

/*
 Copyright 2009 Wallace Wadge

This file is part of BoneCP.

BoneCP is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BoneCP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jolbox.benchmark;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.logicalcobwebs.proxool.ProxoolDataSource;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 *  Benchmarks of C3P0, DBCP and BoneCP.
 *
 * @author wwadge
 * @version $Revision$
 */
@SuppressWarnings("all")
public class BenchmarkTests {


	/** A dummy query for DB. */
	public static final String TEST_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";

	/** Constant. */
	private static final String password = "";
	/** Constant. */
	private static final String username = "sa";
	/** Constant. */
	private static final String url = "jdbc:mock";
	/** Max connections for single. */
	private static final int MAX_CONNECTIONS = 1000000;
	/** Placeholder for all the results. */
	private static List<String> results = new LinkedList<String>();
	/** config setting. */
	public static int threads = 500;
	/** config setting. */
	public static int stepping = 20;
	/** config setting. */
	public static int pool_size = 200;
	/** config setting. */
	public static int helper_threads = 5;
	/** config setting. */
	private int max_idle_time = 0;
	/** config setting. */
	private int idle_connection_test_period = 0;
	/** config setting. */
	private int max_statement = 10;
	/** config setting. */
	private int statement_release_helper = 15;
	/** config setting. */
	private int acquire_increment = 5;
	
	/** 
	 *
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private ComboPooledDataSource multiThreadedC3P0(boolean doPreparedStatement) throws PropertyVetoException, InterruptedException, SQLException {
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");

		cpds.setJdbcUrl(url);
		cpds.setUser(username);
		cpds.setPassword(password);
		cpds.setMaxIdleTime(max_idle_time);
		cpds.setMaxIdleTimeExcessConnections(0);
		cpds.setIdleConnectionTestPeriod(idle_connection_test_period);
		cpds.setMaxConnectionAge(0);
		if (doPreparedStatement){
			cpds.setMaxStatements(max_statement);
		} else {
			cpds.setMaxStatements(0);
		}
		cpds.setMinPoolSize(pool_size); 
		cpds.setMaxPoolSize(pool_size);
		cpds.setNumHelperThreads(helper_threads);
		return cpds;
	}

	/** 
	 *
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private ProxoolDataSource multiThreadedProxool(boolean doPreparedStatement) throws PropertyVetoException, InterruptedException, SQLException {
		ProxoolDataSource cpds = new ProxoolDataSource();
		cpds.setDriver("com.jolbox.bonecp.MockJDBCDriver");

		cpds.setDriverUrl(url);
		cpds.setUser(username);
		cpds.setPassword(password);
		cpds.setMaximumConnectionLifetime(Integer.MAX_VALUE);
		cpds.setMaximumActiveTime(Long.MAX_VALUE);
		cpds.setTestBeforeUse(false);
		cpds.setMinimumConnectionCount(pool_size);
		cpds.setMaximumConnectionCount(pool_size);
		return cpds;
	}


	/**
	 * 
	 *
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private DataSource multiThreadedDBCP(boolean doPreparedStatement) throws PropertyVetoException, InterruptedException, SQLException {
		BasicDataSource cpds = new BasicDataSource();
		cpds.setDriverClassName("com.jolbox.bonecp.MockJDBCDriver");
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		cpds.setMaxIdle(-1);
		cpds.setMinIdle(-1);
		if (doPreparedStatement){
			cpds.setPoolPreparedStatements(true);
			cpds.setMaxOpenPreparedStatements(max_statement);
		}
		cpds.setInitialSize(pool_size);
		cpds.setMaxActive(pool_size);
		return cpds;
	}


	/**
	 * 
	 *
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private DataSource multiThreadedTomcatJDBC(boolean doPreparedStatement) throws PropertyVetoException, InterruptedException, SQLException {
		PoolProperties config = new PoolProperties();
		config.setUrl(url);
		  config.setDriverClassName("com.jolbox.bonecp.MockJDBCDriver");
		config.setUsername(username);
		config.setPassword(password);
		config.setMaxIdle(pool_size);
		config.setMaxAge(0);
		config.setInitialSize(pool_size);
		config.setMaxActive(pool_size);
		org.apache.tomcat.jdbc.pool.DataSource dsb = new org.apache.tomcat.jdbc.pool.DataSource();
          dsb.setPoolProperties(config); 
          if (doPreparedStatement){
        	  // not yet implemented in this version
          }

return dsb;
		
		
		
	}
	
	/**
	 * 
	 *
	 * @param doPreparedStatement 
	 * @param partitions 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private BoneCPDataSource multiThreadedBoneCP(boolean doPreparedStatement, int partitions) throws PropertyVetoException, InterruptedException, SQLException {

		BoneCPDataSource dsb = new BoneCPDataSource();
		dsb.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");
		dsb.setJdbcUrl(url);
		dsb.setUsername(username);
		dsb.setPassword(password);
		dsb.setIdleMaxAge(0L);
		dsb.setIdleConnectionTestPeriod(0L);
		dsb.setDisableConnectionTracking(true);
		if (doPreparedStatement){
			dsb.setStatementsCacheSize(max_statement);
		} else {
			dsb.setStatementsCacheSize(0);
		}
		dsb.setMinConnectionsPerPartition(pool_size / partitions);
		dsb.setMaxConnectionsPerPartition(pool_size / partitions);
		dsb.setPartitionCount(partitions);
		dsb.setReleaseHelperThreads(helper_threads);
		dsb.setStatementReleaseHelperThreads(15);
		dsb.setAcquireIncrement(5);
		return dsb;

	}


	/**
	 * 
	 *
	 * @return time taken
	 * @throws SQLException
	 */
	private long singleBoneCP() throws SQLException{
		// Start BoneCP
		BoneCPConfig config = new BoneCPConfig();
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(password);
		config.setIdleConnectionTestPeriod(0);
		config.setIdleMaxAge(0);
		config.setStatementsCacheSize(0);
		config.setMinConnectionsPerPartition(pool_size);
		config.setMaxConnectionsPerPartition(pool_size);
		config.setPartitionCount(1);
		config.setAcquireIncrement(5);
		config.setDisableConnectionTracking(true);
		config.setReleaseHelperThreads(helper_threads);
		config.setStatementReleaseHelperThreads(statement_release_helper);
		BoneCP dsb = new BoneCP(config);

		long start = System.currentTimeMillis();
		for (int i=0; i < MAX_CONNECTIONS; i++){
			Connection conn = dsb.getConnection();
			conn.close();
		}
		long end = (System.currentTimeMillis() - start);
		//		System.out.println("BoneCP Single thread benchmark: "+end);


		dsb.shutdown();
		return end;

	}
	/**
	 * 
	 *
	 * @return time taken
	 * @throws SQLException
	 */
	private long singleTomcatJDBC() throws SQLException{
		// Start tomcat JDBC
		PoolProperties config = new PoolProperties();
		config.setUrl(url);
		  config.setDriverClassName("com.jolbox.bonecp.MockJDBCDriver");
		config.setUsername(username);
		config.setPassword(password);
		config.setMaxIdle(pool_size);
		config.setMaxAge(0);
		config.setInitialSize(pool_size);
		config.setMaxActive(pool_size);
		org.apache.tomcat.jdbc.pool.DataSource dsb = new org.apache.tomcat.jdbc.pool.DataSource();
          dsb.setPoolProperties(config); 

		long start = System.currentTimeMillis();
		for (int i=0; i < MAX_CONNECTIONS; i++){
			Connection conn = dsb.getConnection();
			conn.close();
		}
		long end = (System.currentTimeMillis() - start);


		dsb.close();
		return end;

	}


	/**
	 * 
	 *
	 * @return time taken
	 * @throws SQLException
	 */
	private long singleDBCP() throws SQLException{
		// Start DBCP

		BasicDataSource cpds = new BasicDataSource();
		cpds.setDriverClassName("com.jolbox.bonecp.MockJDBCDriver");
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		cpds.setMaxIdle(-1);
		cpds.setMinIdle(-1);
		cpds.setMaxOpenPreparedStatements(max_statement);
		cpds.setInitialSize(pool_size);
		cpds.setMaxActive(pool_size);
		cpds.getConnection(); // call to initialize possible lazy structures etc 


		long start = System.currentTimeMillis();
		for (int i=0; i < MAX_CONNECTIONS; i++){
			Connection conn = cpds.getConnection();
			conn.close();
		}
		long end = (System.currentTimeMillis() - start);
		//		System.out.println("DBCP Single thread benchmark: "+end);


		cpds.close();
		return end;

	}



	/**
	 * 
	 *
	 * @return time taken
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	private long singleC3P0() throws SQLException, PropertyVetoException{
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setDriverClass("org.hsqldb.jdbcDriver");

		cpds.setJdbcUrl(url);
		cpds.setUser(username);
		cpds.setPassword(password);
		cpds.setMaxIdleTime(0);
		cpds.setMaxIdleTimeExcessConnections(0);
		cpds.setIdleConnectionTestPeriod(0);
		cpds.setMaxConnectionAge(0);
		cpds.setMaxStatements(0);
		cpds.setMinPoolSize(pool_size);
		cpds.setMaxPoolSize(pool_size);
		cpds.setAcquireIncrement(acquire_increment );
		cpds.setNumHelperThreads(1);
		long start = System.currentTimeMillis();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Connection conn = cpds.getConnection();
			conn.close();
		}

		long end = (System.currentTimeMillis() - start);
//		System.out.println("C3P0 Single thread benchmark: "+end);
		// dispose of pool
		DataSources.destroy(cpds);
		return end;
	}




	/**
	 * 
	 *
	 * @return result
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	public long[] testSingleThread() throws SQLException, PropertyVetoException{
		System.out.println("Single Thread get/release connection");

		long[] results = new long[ConnectionPoolType.values().length];

		for (ConnectionPoolType poolType: ConnectionPoolType.values()){
			if (!poolType.isEnabled() && !poolType.isMultiPartitions()){
				continue;
			}
			System.out.println("|- Benchmarking " + poolType);
			int cycles = 3;
			long[] cycleResults  = new long[cycles];

			for (int i=0; i < cycles; i++){
				switch(poolType){
				case C3P0:
					cycleResults[i]=singleC3P0();
					break;
				case DBCP:
					cycleResults[i]=singleDBCP();
					break;
				case BONECP_1_PARTITIONS: 		
					cycleResults[i]=singleBoneCP();
					break;
				case TOMCAT_JDBC:
					cycleResults[i]=singleTomcatJDBC();
				default: 
					System.err.println("Unknown");
				}

			}

			long total = 0;
			for (int i=0; i < cycles; i++) {
				total += cycleResults[i];
			}

			results[poolType.ordinal()] = total / cycles;
		}

		return results;
	}

	/**
	 * 
	 *
	 * @return  result
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	public long testSingleThreadDBCP() throws SQLException, PropertyVetoException{
		int cycles = 3;
		long[] dbcpResults  = new long[cycles];

		for (int i=0; i < cycles; i++){
			dbcpResults[i]=singleDBCP();
		}


		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += dbcpResults[i];
		}

		long result = total / cycles;
		//		System.out.println("DBCP Average = " + result);
		//		results.add("DBCP, "+result);
		return result;
	}

	/**
	 * 
	 *
	 * @return result
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	public long testSingleThreadBoneCP() throws SQLException, PropertyVetoException{
		int cycles = 3;
		long[] boneCPResults = new long[cycles];

		for (int i=0; i < cycles; i++){
			boneCPResults[i]=singleBoneCP();
		}

		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += boneCPResults[i];
		}

		long result = total / cycles;
		//		System.out.println("BoneCP Average = " + result);
		return result;
	}


	/**
	 * 
	 * 
	 * @param delay 
	 * @param doStatements 
	 * @return result
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws NoSuchMethodException  
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	public long[][] testMultiThreadedConstantDelay(int delay, boolean doStatements) throws SQLException, PropertyVetoException, InterruptedException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		System.out.println("Multithreading test ("+delay+"ms work delay per thread). DoStatements = " + doStatements);
		long[][] finalResults = new long[ConnectionPoolType.values().length][threads]; 

		for (ConnectionPoolType poolType: ConnectionPoolType.values()){
			if (!poolType.isEnabled()){
				continue;
			}
			System.out.println("|- Benchmarking "+poolType);
			finalResults[poolType.ordinal()] = multiThreadTest(delay, doStatements, poolType);
		}
		return finalResults;
	}

	/**
	 * @param delay
	 * @return result
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public long[][] testMultiThreadedConstantDelay(int delay) throws SQLException, PropertyVetoException, InterruptedException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		return testMultiThreadedConstantDelay(delay, false);
	}

	/**
	 * @param delay
	 * @return result
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public long[][] testMultiThreadedConstantDelayWithPreparedStatements(int delay) throws SQLException, PropertyVetoException, InterruptedException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		return testMultiThreadedConstantDelay(delay, true);
	}

	/**
	 * 
	 *
	 * @param workdelay
	 * @param doPreparedStatement
	 * @param poolType 
	 * @return result
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws SQLException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */

	private long[] multiThreadTest(int workdelay, boolean doPreparedStatement, ConnectionPoolType poolType) throws PropertyVetoException,
	InterruptedException, SQLException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {


		int numCycles = 10;
		long[] tempResults  = new long[numCycles];

		long[] poolResults  = new long[threads];
		DataSource ds = null;
		switch (poolType) {
		case BONECP_1_PARTITIONS: 			
			ds=multiThreadedBoneCP(doPreparedStatement, 1);
			break;
		case BONECP_2_PARTITIONS: 			
			ds=multiThreadedBoneCP(doPreparedStatement,2);
			break;
		case BONECP_4_PARTITIONS: 			
			ds=multiThreadedBoneCP(doPreparedStatement,4);
			break;
		case BONECP_5_PARTITIONS: 			
			ds=multiThreadedBoneCP(doPreparedStatement,5);
			break;
		case BONECP_10_PARTITIONS: 			
			ds=multiThreadedBoneCP(doPreparedStatement,10);
			break;

		case C3P0: 			
			ds=multiThreadedC3P0(doPreparedStatement);
			break;
		case PROXOOL: 			
			ds=multiThreadedProxool(doPreparedStatement);
			break;

		case DBCP:
			ds = multiThreadedDBCP(doPreparedStatement);
			break;
		case TOMCAT_JDBC:
			ds = multiThreadedTomcatJDBC(doPreparedStatement);
			break;
		default:
			break;
		}


		for (int threadCount=1; threadCount <= threads; threadCount=threadCount+stepping){

			for (int cycle=0; cycle < numCycles; cycle++){
				if (ds == null){
					continue;
				}
				tempResults[cycle]=(long) (startThreadTest(threadCount, ds, workdelay, doPreparedStatement)/(1.0*threadCount));
			}


			long min = Long.MAX_VALUE;
			for (int i=0; i < numCycles; i++) {
				min = Math.min(min, tempResults[i]);
			}

			//			String result = poolType+", "+threadCount + ", "+min;
			poolResults[threadCount]=min;
			//			System.out.println(result);
			//			results.add(result);
		}
		if (ds != null){
			ds.getClass().getMethod("close").invoke(ds);
		}
		return poolResults;
	}


	/**
	 * Benchmarks PreparedStatement functionality (single thread) 
	 * @return result
	 * 
	 * @throws PropertyVetoException
	 * @throws SQLException
	 */
	private long testPreparedStatementSingleThreadC3P0() throws PropertyVetoException, SQLException{
		results.add("PreparedStatement (single threaded), time (ms)");
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setDriverClass("com.jolbox.bonecp.MockJDBCDriver");

		cpds.setJdbcUrl(url);
		cpds.setUser(username);
		cpds.setPassword(password);
		cpds.setMaxIdleTime(0);
		cpds.setMaxIdleTimeExcessConnections(0);
		cpds.setIdleConnectionTestPeriod(0);
		cpds.setMaxConnectionAge(0);
		cpds.setMaxStatements(30);
		cpds.setMaxStatementsPerConnection(30);
		cpds.setMinPoolSize(pool_size);
		cpds.setMaxPoolSize(pool_size);
		cpds.setAcquireIncrement(5);
		cpds.setNumHelperThreads(helper_threads);
		Connection conn = cpds.getConnection();
		long start = System.currentTimeMillis();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Statement st = conn.prepareStatement(TEST_QUERY);
			st.close();
		}
		conn.close();

		long end = (System.currentTimeMillis() - start);
		System.out.println("C3P0 PreparedStatement Single thread benchmark: "+end);
		results.add("C3P0, "+end);
		// dispose of pool
		DataSources.destroy(cpds);
		return end;
	}

	/**
	 * Benchmarks PreparedStatement functionality (single thread) 
	 * @return result
	 * 
	 * @throws PropertyVetoException
	 * @throws SQLException
	 */
	private long testPreparedStatementSingleThreadDBCP() throws PropertyVetoException, SQLException{
		BasicDataSource cpds = new BasicDataSource();
		cpds.setDriverClassName("com.jolbox.bonecp.MockJDBCDriver");
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		cpds.setMaxIdle(-1);
		cpds.setMinIdle(-1);
		cpds.setPoolPreparedStatements(true);
		cpds.setMaxOpenPreparedStatements(30);
		cpds.setInitialSize(pool_size);
		cpds.setMaxActive(pool_size);
		Connection conn = cpds.getConnection(); 

		long start = System.currentTimeMillis();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Statement st = conn.prepareStatement(TEST_QUERY);
			st.close();
		}
		conn.close();

		long end = (System.currentTimeMillis() - start);
		System.out.println("DBCP PreparedStatement Single thread benchmark: "+end);
		results.add("DBCP, "+end);
		// dispose of pool
		cpds.close();
		return end;
	}


	/**
	 * Benchmarks PreparedStatement functionality (single thread) 
	 * @return result
	 * 
	 * @throws PropertyVetoException
	 * @throws SQLException
	 */
	private long testPreparedStatementSingleThreadBoneCP() throws PropertyVetoException, SQLException{
		// Start BoneCP
		BoneCPConfig config = new BoneCPConfig();
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(password);
		config.setIdleConnectionTestPeriod(0);
		config.setIdleMaxAge(0);
		config.setStatementsCacheSize(30);
		config.setDisableConnectionTracking(true);
		config.setMinConnectionsPerPartition(pool_size);
		config.setMaxConnectionsPerPartition(pool_size);
		config.setPartitionCount(1);
		config.setAcquireIncrement(5);
		config.setReleaseHelperThreads(helper_threads);
		config.setStatementReleaseHelperThreads(15);
		
		BoneCP dsb = new BoneCP(config);

		Connection conn = dsb.getConnection();

		long start = System.currentTimeMillis();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Statement st = conn.prepareStatement(TEST_QUERY);
			st.close();
		}
		conn.close();

		long end = (System.currentTimeMillis() - start);
//		System.out.println("BoneCP PreparedStatement Single thread benchmark: "+end);
//		results.add("BoneCP (1 partitions), "+end);
		// dispose of pool
		dsb.shutdown();
//		results.add("");
		return end;
	}


	/**
	 * 
	 *
	 * @return results
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	public long[] testPreparedStatementSingleThread() throws SQLException, PropertyVetoException{
		System.out.println("Single Thread get/release connection using preparedStatements");

		long[] results = new long[ConnectionPoolType.values().length];

		for (ConnectionPoolType poolType: ConnectionPoolType.values()){
			if (!poolType.isEnabled() && !poolType.isMultiPartitions()){
				continue;
			}
			System.out.println("|- Benchmarking " + poolType);
			int cycles = 3;
			long[] cycleResults  = new long[cycles];

			for (int i=0; i < cycles; i++){
				switch(poolType){
				case C3P0:
					cycleResults[i]=testPreparedStatementSingleThreadC3P0();
					break;
				case DBCP:
					cycleResults[i]=testPreparedStatementSingleThreadDBCP();
					break;
				case BONECP_1_PARTITIONS: 		
					cycleResults[i]=testPreparedStatementSingleThreadBoneCP();
					break;
				default: 
					System.err.println("Unknown");
				}

			}

			long total = 0;
			for (int i=0; i < cycles; i++) {
				total += cycleResults[i];
			}

			results[poolType.ordinal()] = total / cycles;
		}

		return results;
	}

	/**
	 * Helper function.
	 *
	 * @param threads
	 * @param cpds
	 * @param workDelay
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws InterruptedException
	 */
	public static long startThreadTest(int threads,
			DataSource cpds, int workDelay, boolean doPreparedStatement) throws InterruptedException {
		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch doneSignal = new CountDownLatch(threads);

		ExecutorService pool = Executors.newFixedThreadPool(threads);
		ExecutorCompletionService<Long> ecs = new ExecutorCompletionService<Long>(pool);
		for (int i = 0; i <= threads; i++){ // create and start threads
			ecs.submit(new ThreadTesterUtil(startSignal, doneSignal, cpds, workDelay, doPreparedStatement));
		}

		startSignal.countDown(); // START TEST!
		doneSignal.await(); 
		long time = 0;
		for (int i = 0; i <= threads; i++){
			try {
				time = time + ecs.take().get();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		pool.shutdown();
		return time;
	}
}

