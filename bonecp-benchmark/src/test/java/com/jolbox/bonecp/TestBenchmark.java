package com.jolbox.bonecp;

import java.beans.PropertyVetoException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.CommonUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 *  Benchmarks of C3P0, DBCP and BoneCP.
 *
 * @author wwadge
 * @version $Revision$
 */
public class TestBenchmark {
	/** */
	private static final String password = "";
	/** */
	private static final String username = "sa";
	/** */
	private static final String url = "jdbc:hsqldb:mem:test";
	/** Max connections for single. */
	private static final int MAX_CONNECTIONS = 1000000;
	/** Placeholder for all the results. */
	private static List<String> results = new LinkedList<String>();
	/**
	 * 1-time bootstrapping 
	 *
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void setup() throws ClassNotFoundException{
		Class.forName("org.hsqldb.jdbcDriver" );
	}





	/** 
	 *
	 * @param connections no of connections
	 * @param workdelay 
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private long multiThreadedC3P0(int connections, int workdelay, boolean doPreparedStatement) throws PropertyVetoException, InterruptedException, SQLException {
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setDriverClass("org.hsqldb.jdbcDriver");

		cpds.setJdbcUrl(url);
		cpds.setUser(username);
		cpds.setPassword(password);
		cpds.setMaxIdleTime(0);
		cpds.setMaxIdleTimeExcessConnections(0);
		cpds.setIdleConnectionTestPeriod(0);
		cpds.setMaxConnectionAge(0);
		if (doPreparedStatement){
			cpds.setMaxStatements(10);
		} else {
			cpds.setMaxStatements(0);
		}
		cpds.setMinPoolSize(50);
		cpds.setMaxPoolSize(200);
		cpds.setAcquireIncrement(5);
		cpds.setNumHelperThreads(5);
		Connection con = cpds.getConnection(); // call to initialize possible lazy structures etc 
		long result = CommonUtils.startThreadTest(500, connections,  cpds, workdelay, doPreparedStatement);
		// kill off the db...
		String sql = "SHUTDOWN"; // hsqldb interprets this as a request to terminate
		Statement stmt = con.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();
		con.close();
		cpds.close();
		return result;

	}

	
	/**
	 * 
	 *
	 * @param connections 
	 * @param workdelay 
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private long multiThreadedDBCP(int connections, int workdelay, boolean doPreparedStatement) throws PropertyVetoException, InterruptedException, SQLException {
		BasicDataSource cpds = new BasicDataSource();
		cpds.setDriverClassName("org.hsqldb.jdbcDriver");
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		cpds.setMaxIdle(-1);
		cpds.setMinIdle(-1);
		if (doPreparedStatement){
			cpds.setPoolPreparedStatements(true);
			cpds.setMaxOpenPreparedStatements(10);
		}
		cpds.setInitialSize(50);
		cpds.setMaxActive(200);
		Connection con = cpds.getConnection(); // call to initialize possible lazy structures etc 
		long result = CommonUtils.startThreadTest(500, connections,  cpds, workdelay, doPreparedStatement);
		// kill off the db...
		String sql = "SHUTDOWN"; // hsqldb interprets this as a request to terminate
		Statement stmt = con.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();
		con.close();
		cpds.close();
		return result;

	}

	/**
	 * 
	 *
	 * @param partitions 
	 * @param connections 
	 * @param workdelay 
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws PropertyVetoException 
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private long multiThreadedBoneCP(int partitions, int connections, int workdelay, boolean doPreparedStatement) throws PropertyVetoException, InterruptedException, SQLException {

		BoneCPDataSource dsb = new BoneCPDataSource();
		dsb.setDriverClass("org.hsqldb.jdbcDriver");
		dsb.setJdbcUrl(url);
		dsb.setUsername(username);
		dsb.setPassword(password);
		dsb.setIdleMaxAge(0L);
		if (workdelay == 0){
			// make it similar to DBCP for fairness
			dsb.setReleaseHelperThreads(0);
		}
		dsb.setIdleConnectionTestPeriod(0L);
		if (doPreparedStatement){
			dsb.setPreparedStatementCacheSize(10);
		} else {
			dsb.setPreparedStatementCacheSize(0);
		}
		// divide no of avail connections so as to remain constant with the
		// other connection pools
		dsb.setMinConnectionsPerPartition(50/partitions);
		dsb.setMaxConnectionsPerPartition(200/partitions);
		dsb.setAcquireIncrement(5);
		dsb.setPartitionCount(partitions);
		Connection con = dsb.getConnection(); // call to initialize possible lazy structures etc
		long result = CommonUtils.startThreadTest(500, connections,  dsb, workdelay, doPreparedStatement);
		//		return TestUtils.startThreadTest(5, connections,  dsb, workdelay);
		// kill off the db...
		String sql = "SHUTDOWN"; // hsqldb interprets this as a request to terminate
		Statement stmt = con.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();
		con.close();
		dsb.close();

		return result;

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
		config.setPreparedStatementsCacheSize(0);
		config.setMinConnectionsPerPartition(20);
		config.setMaxConnectionsPerPartition(50);
		config.setAcquireIncrement(5);
		config.setPartitionCount(1);
		config.setReleaseHelperThreads(0);
		BoneCP dsb = new BoneCP(config);

		long start = System.currentTimeMillis();
		for (int i=0; i < MAX_CONNECTIONS; i++){
			Connection conn = dsb.getConnection();
			conn.close();
		}
		long end = (System.currentTimeMillis() - start);
		System.out.println("BoneCP Single thread benchmark: "+end);


		dsb.shutdown();
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
		cpds.setDriverClassName("org.hsqldb.jdbcDriver");
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		cpds.setMaxIdle(-1);
		cpds.setMinIdle(-1);
		cpds.setMaxOpenPreparedStatements(0);
		cpds.setInitialSize(20);
		cpds.setMaxActive(50);
		cpds.getConnection(); // call to initialize possible lazy structures etc 


		long start = System.currentTimeMillis();
		for (int i=0; i < MAX_CONNECTIONS; i++){
			Connection conn = cpds.getConnection();
			conn.close();
		}
		long end = (System.currentTimeMillis() - start);
		System.out.println("DBCP Single thread benchmark: "+end);


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
		cpds.setMinPoolSize(20);
		cpds.setMaxPoolSize(50);
		cpds.setAcquireIncrement(5);
		cpds.setNumHelperThreads(1);
		long start = System.currentTimeMillis();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Connection conn = cpds.getConnection();
			conn.close();
		}

		long end = (System.currentTimeMillis() - start);
		System.out.println("C3P0 Single thread benchmark: "+end);
		// dispose of pool
		DataSources.destroy(cpds);
		return end;
	}




	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	@Test
	public void testSingleThreadC3P0() throws SQLException, PropertyVetoException{
		results.add("Single Thread, time (ms)");
		int cycles = 3;
		long[] c3p0Results  = new long[cycles];

		for (int i=0; i < cycles; i++){
			c3p0Results[i]=singleC3P0();
		}

		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += c3p0Results[i];
		}

		System.out.println("C3P0 Average = " + total / cycles);
		results.add("C3P0, "+total / cycles);
	}

	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	@Test
	public void testSingleThreadDBCP() throws SQLException, PropertyVetoException{
		int cycles = 3;
		long[] dbcpResults  = new long[cycles];

		for (int i=0; i < cycles; i++){
			dbcpResults[i]=singleDBCP();
		}


		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += dbcpResults[i];
		}

		System.out.println("DBCP Average = " + total / cycles);
		results.add("DBCP, "+total / cycles);
	}

	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	@Test
	public void testSingleThreadBoneCP() throws SQLException, PropertyVetoException{
		int cycles = 3;
		long[] boneCPResults = new long[cycles];

		for (int i=0; i < cycles; i++){
			boneCPResults[i]=singleBoneCP();
		}

		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += boneCPResults[i];
		}

		System.out.println("BoneCP Average = " + total / cycles);
		results.add("BoneCP, "+total / cycles);
		results.add("");
	}


	
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedNoDelayC3P0() throws SQLException, PropertyVetoException, InterruptedException{
		results.add("Multi Thread (no delay), time (ms)");
		System.out.println("Multithreading test (0ms work delay per thread)");
		multiThreadTestC3P0(0, false);
		System.out.println("--------------------------------");
	}

	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedNoDelayDBCP() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("Multithreading test (0ms work delay per thread)");
		multiThreadTestDBCP(0, false);
		System.out.println("--------------------------------");
	}

	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedNoDelayBoneCP_1Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("Multithreading test (0ms work delay per thread)");
		multiThreadTestBoneCP(1,0, false);
		System.out.println("--------------------------------");
	}
	
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedNoDelayBoneCP_2Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("Multithreading test (0ms work delay per thread)");
		multiThreadTestBoneCP(2,0, false);
		System.out.println("--------------------------------");
	}
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedNoDelayBoneCP_4Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("Multithreading test (0ms work delay per thread)");
		multiThreadTestBoneCP(4,0, false);
		System.out.println("--------------------------------");
	}
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedNoDelayBoneCP_5Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("Multithreading test (0ms work delay per thread)");
		multiThreadTestBoneCP(5,0, false);
		System.out.println("--------------------------------");
	}
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedNoDelayBoneCP_10Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("Multithreading test (0ms work delay per thread)");
		multiThreadTestBoneCP(10, 0, false);
		System.out.println("--------------------------------");
		results.add("");
	}

	
	/**
	 * 
	 *
	 * @param partitions
	 * @param workdelay
	 * @param doPreparedStatement
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private void multiThreadTestBoneCP(int partitions, int workdelay, boolean doPreparedStatement) throws PropertyVetoException,
	InterruptedException, SQLException {
		int cycles = 3;

		long[] boneCPResults = new long[cycles];

		int connections = 100;

		for (int i=0; i < cycles; i++){
			System.out.println("BoneCP ("+partitions+" partitions) cycle "+ i+ ": ");
			boneCPResults[i]=multiThreadedBoneCP(partitions, connections, workdelay, doPreparedStatement);
		}

		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += boneCPResults[i];
		}

		System.out.println("BoneCP MultiThread ("+partitions+" partition) Average = " + total / cycles);
		if (doPreparedStatement){
			results.add("BoneCP ("+partitions + " partitions), "+total / cycles);
		} else {
			results.add("BoneCP ("+partitions+" partition), "+total / cycles);

		}

	}


	/**
	 * 
	 *
	 * @param workdelay
	 * @param doPreparedStatement
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private void multiThreadTestDBCP(int workdelay, boolean doPreparedStatement) throws PropertyVetoException,
	InterruptedException, SQLException {
		int cycles = 3;

		long[] dbcpResults  = new long[cycles];

		int connections = 100;

		for (int i=0; i < cycles; i++){
			System.out.println("[dbcp] Cycle " + i+": ");
			dbcpResults[i]=multiThreadedDBCP(connections, workdelay, doPreparedStatement);
		}

		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += dbcpResults[i];
		}

		System.out.println("DBCP MultiThread Average = " + total / cycles);
		if (doPreparedStatement){
			results.add("DBCP, "+total / cycles);
		} else {
			results.add("DBCP, "+total / cycles);
		}
	}


	/**
	 * 
	 *
	 * @param workdelay
	 * @param doPreparedStatement
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private void multiThreadTestC3P0(int workdelay, boolean doPreparedStatement) throws PropertyVetoException,
	InterruptedException, SQLException {
		int cycles = 3;

		long[] c3p0Results  = new long[cycles];

		int connections = 100;

		for (int i=0; i < cycles; i++){
			System.out.println("[c3p0] Cycle " + i+": ");
			c3p0Results[i]=multiThreadedC3P0(connections, workdelay, doPreparedStatement);
		}

		long total = 0;
		for (int i=0; i < cycles; i++) {
			total += c3p0Results[i];
		}

		System.out.println("C3P0 MultiThread Average = " + total / cycles);
		if (doPreparedStatement){
			results.add("C3P0, "+total / cycles);

		} else {
			results.add("C3P0, "+total / cycles);
		}
	}

	
		/**
	 * Benchmarks PreparedStatement functionality (single thread) 
	 * 
	 * @throws PropertyVetoException
	 * @throws SQLException
	 */
	@Test
	public void testPreparedStatementSingleThreadC3P0() throws PropertyVetoException, SQLException{
		results.add("PreparedStatement (single threaded), time (ms)");
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setDriverClass("org.hsqldb.jdbcDriver");

		cpds.setJdbcUrl(url);
		cpds.setUser(username);
		cpds.setPassword(password);
		cpds.setMaxIdleTime(0);
		cpds.setMaxIdleTimeExcessConnections(0);
		cpds.setIdleConnectionTestPeriod(0);
		cpds.setMaxConnectionAge(0);
		cpds.setMaxStatements(30);
		cpds.setMaxStatementsPerConnection(30);
		cpds.setMinPoolSize(20);
		cpds.setMaxPoolSize(50);
		cpds.setAcquireIncrement(5);
		cpds.setNumHelperThreads(5);
		long start = System.currentTimeMillis();
		Connection conn = cpds.getConnection();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Statement st = conn.prepareStatement(CommonUtils.TEST_QUERY);
			st.close();
		}
		conn.close();

		long end = (System.currentTimeMillis() - start);
		System.out.println("C3P0 PreparedStatement Single thread benchmark: "+end);
		results.add("C3P0, "+end);
		// dispose of pool
		DataSources.destroy(cpds);
	}

	/**
	 * Benchmarks PreparedStatement functionality (single thread) 
	 * 
	 * @throws PropertyVetoException
	 * @throws SQLException
	 */
	@Test
	public void testPreparedStatementSingleThreadDBCP() throws PropertyVetoException, SQLException{
		BasicDataSource cpds = new BasicDataSource();
		cpds.setDriverClassName("org.hsqldb.jdbcDriver");
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		cpds.setMaxIdle(-1);
		cpds.setMinIdle(-1);
		cpds.setPoolPreparedStatements(true);
		cpds.setMaxOpenPreparedStatements(30);
		cpds.setInitialSize(20);
		cpds.setMaxActive(50);
		Connection conn = cpds.getConnection(); 

		long start = System.currentTimeMillis();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Statement st = conn.prepareStatement(CommonUtils.TEST_QUERY);
			st.close();
		}
		conn.close();

		long end = (System.currentTimeMillis() - start);
		System.out.println("DBCP PreparedStatement Single thread benchmark: "+end);
		results.add("DBCP, "+end);
		// dispose of pool
		cpds.close();
	}

	/**
	 * Benchmarks PreparedStatement functionality (single thread) 
	 * 
	 * @throws PropertyVetoException
	 * @throws SQLException
	 */
	@Test
	public void testPreparedStatementSingleThreadBoneCP() throws PropertyVetoException, SQLException{
		// Start BoneCP
		BoneCPConfig config = new BoneCPConfig();
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(password);
		config.setIdleConnectionTestPeriod(0);
		config.setIdleMaxAge(0);
		config.setPreparedStatementsCacheSize(30);
		config.setMinConnectionsPerPartition(20);
		config.setMaxConnectionsPerPartition(50);
		config.setAcquireIncrement(5);
		config.setPartitionCount(1);
		config.setReleaseHelperThreads(5);
		BoneCP dsb = new BoneCP(config);

		Connection conn = dsb.getConnection();

		long start = System.currentTimeMillis();
		for (int i=0; i< MAX_CONNECTIONS; i++){
			Statement st = conn.prepareStatement(CommonUtils.TEST_QUERY);
			st.close();
		}
		conn.close();

		long end = (System.currentTimeMillis() - start);
		System.out.println("BoneCP PreparedStatement Single thread benchmark: "+end);
		results.add("BoneCP (1 partitions), "+end);
		// dispose of pool
		dsb.shutdown();
		results.add("");
	}

	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedPreparedStatementC3P0() throws SQLException, PropertyVetoException, InterruptedException{
		results.add("PreparedStatement (Multithreading threaded, no delay), time (ms)");
		System.out.println("C3P0 Multithreading PreparedStatement test (0ms work delay per thread)");
		multiThreadTestC3P0(0, true);
		System.out.println("--------------------------------");
	}


	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedPreparedStatementDBCP() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("DBCP Multithreading PreparedStatement test (0ms work delay per thread)");
		multiThreadTestDBCP(0, true);
		System.out.println("--------------------------------");
	}

	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedPreparedStatementBoneCP_1Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("BoneCP Multithreading PreparedStatement test (0ms work delay per thread)");
		multiThreadTestBoneCP(1, 0, true);
		System.out.println("--------------------------------");
	}
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedPreparedStatementBoneCP_2Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("BoneCP Multithreading PreparedStatement test (0ms work delay per thread)");
		multiThreadTestBoneCP(2, 0, true);
		System.out.println("--------------------------------");
	}
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedPreparedStatementBoneCP_4Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("BoneCP Multithreading PreparedStatement test (0ms work delay per thread)");
		multiThreadTestBoneCP(4, 0, true);
		System.out.println("--------------------------------");
	}
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedPreparedStatementBoneCP_5Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("BoneCP Multithreading PreparedStatement test (0ms work delay per thread)");
		multiThreadTestBoneCP(5, 0, true);
		System.out.println("--------------------------------");
	}
	/**
	 * 
	 *
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedPreparedStatementBoneCP_10Partition() throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("BoneCP Multithreading PreparedStatement test (0ms work delay per thread)");
		multiThreadTestBoneCP(10, 0, true);
		System.out.println("--------------------------------");
		results.add("");
	}

	
	/** Helper method.
	 * @param delay
	 * @throws SQLException
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 */
	public void invokeMultiThreadedPreparedStatementWithDelay(int delay) throws SQLException, PropertyVetoException, InterruptedException{
		System.out.println("PreparedStatement multi-threaded (" + delay + "ms delay)");
		results.add("PreparedStatement (multi-threaded with "+delay+"ms delay), time (ms)");
		multiThreadTestC3P0(delay, true);
		multiThreadTestDBCP(delay, true);
		multiThreadTestBoneCP(1, delay, true);
		multiThreadTestBoneCP(2, delay, true);
		multiThreadTestBoneCP(4, delay, true);
		multiThreadTestBoneCP(5, delay, true);
		multiThreadTestBoneCP(10, delay, true);
		results.add("");	
	}

	/** Benchmark method.
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test
	public void testPreparedStatementWithDelay() throws PropertyVetoException, InterruptedException, SQLException{
		
		invokeMultiThreadedPreparedStatementWithDelay(10);
		invokeMultiThreadedPreparedStatementWithDelay(25);
		invokeMultiThreadedPreparedStatementWithDelay(50);
		invokeMultiThreadedPreparedStatementWithDelay(75);
	}

	/** Helper method.
	 * @param delay
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	public void invokeMultiThreadedWithDelay(int delay) throws PropertyVetoException, InterruptedException, SQLException{
		System.out.println("MultiThreaded (" + delay + "ms delay), time (ms)");
		results.add("MultiThreaded ("+delay+"ms delay), time (ms)");
		multiThreadTestC3P0(delay,false);
		multiThreadTestDBCP(delay, false);
		multiThreadTestBoneCP(1,delay, false);
		multiThreadTestBoneCP(2, delay, false);
		multiThreadTestBoneCP(4, delay, false);
		multiThreadTestBoneCP(5, delay, false);
		multiThreadTestBoneCP(10, delay, false);
		results.add("");
	}

	/** Multi-Threaded + various delays.
	 * @throws PropertyVetoException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test
	public void testMultiThreadedWithDelay() throws PropertyVetoException, InterruptedException, SQLException{
		invokeMultiThreadedWithDelay(10);
		invokeMultiThreadedWithDelay(25);
		invokeMultiThreadedWithDelay(50);
		invokeMultiThreadedWithDelay(75);
	}
	
	/**
	 * @throws IOException 
	 * 
	 *
	 */
	@After
	public void printResults() throws IOException{
		System.out.println("\n\n\nCurrent results");
		System.out.println("--------------------");

		PrintWriter out = new PrintWriter(new FileWriter("benchmarkresults.csv", false), true);

		for (String res: results){
			System.out.println(res);
			out.println(res);
		}
		
		out.close();
		System.out.println("results written to benchmarkresults.csv");
	}

}
