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
package com.jolbox.bonecp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author wallacew
 *
 */
public class TestStressTest {

	/** Just a random blob to test bonecp. 
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	@Test
	public void testStress() throws SQLException, InterruptedException{
		new MockJDBCDriver(new MockJDBCAnswer() {
			
			public Connection answer() throws SQLException {
				return new MockConnection();
			}
		});
		
		BoneCPConfig config = new BoneCPConfig();
//		config.setDisableConnectionTracking(false);
		config.setMinConnectionsPerPartition(40);
		config.setMaxConnectionsPerPartition(100);
		config.setPartitionCount(1);
		config.setMaxConnectionAgeInSeconds(5);
		config.setIdleMaxAgeInSeconds(1);
		config.setUsername("foo");
		config.setPassword("foo");
		config.setJdbcUrl("jdbc:mock");
		config.setStatementsCacheSize(100);
		config.setStatementReleaseHelperThreads(3);
		config.setReleaseHelperThreads(3);
		config.setCloseConnectionWatch(true);
		config.setServiceOrder("LIFO");
		final BoneCP pool = new BoneCP(config);
		final Random rand = new Random();
		while (true){
		final AtomicInteger count = new AtomicInteger();
		for (int i=0; i < 200; i++){
			Thread t = 
			new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Connection c = pool.getConnection();
//					Thread.sleep(rand.nextInt(50));
					PreparedStatement ps = c.prepareStatement("FOO");
//					Thread.sleep(rand.nextInt(50));
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
