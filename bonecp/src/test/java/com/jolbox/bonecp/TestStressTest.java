package com.jolbox.bonecp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

public class TestStressTest {

	@Test
	@Ignore
	public void testStress() throws SQLException, InterruptedException{
MockJDBCDriver driver = new MockJDBCDriver(new MockJDBCAnswer() {
			
			public Connection answer() throws SQLException {
				return new MockConnection();
			}
		});
		
		BoneCPConfig config = new BoneCPConfig();
//		config.setDisableConnectionTracking(false);
		config.setMinConnectionsPerPartition(40);
		config.setMaxConnectionsPerPartition(100);
		config.setPartitionCount(1);
//		config.setMaxConnectionAgeInSeconds(5);
		config.setIdleMaxAgeInSeconds(1);
		config.setUsername("foo");
		config.setPassword("foo");
		config.setJdbcUrl("jdbc:mock");
		config.setStatementsCacheSize(100);
		final BoneCP pool = new BoneCP(config);
		final Random rand = new Random();
		while (true){
		final AtomicInteger count = new AtomicInteger();
		for (int i=0; i < 200; i++){
			Thread t = 
			new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Connection c = pool.getConnection();
					PreparedStatement ps = c.prepareStatement("FOO");
					Thread.sleep(rand.nextInt(300));
					ps.close();
					c.close();
					c = null;
					System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
					count.incrementAndGet();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		t.start();
		}
		   while (count.get() != 200){
			   Thread.sleep(200);
		   }
		   System.out.println("Restarting...");
		}
//	Thread.sleep(10000);	
	}
}
