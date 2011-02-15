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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;


/** Testclass for statistics module
 * @author wallacew
 *
 */
public class TestStatistics {

	/** pool handle. */
	private BoneCP mockPool = createNiceMock(BoneCP.class);
	/** stats handle. */
	private Statistics stats = new Statistics(this.mockPool);

	

	@Test
	public void testStat2() throws ClassNotFoundException, SQLException, InterruptedException{
		Class.forName("org.hsqldb.jdbcDriver" );
		BoneCPConfig config = new BoneCPConfig();
		config.setTransactionRecoveryEnabled(false);
		config.setJdbcUrl("jdbc:hsqldb:mem");
		config.setUsername("sa");
		config.setPassword("");
		config.setMinConnectionsPerPartition(1);
		config.setMaxConnectionsPerPartition(2);
		config.setPartitionCount(1);
		config.setAcquireRetryAttempts(1);
		config.setAcquireRetryDelayInMs(1);
		config.setDisableConnectionTracking(false);
		config.setMaxConnectionAgeInSeconds(5);
		config.setReleaseHelperThreads(0);
		final BoneCP pool = new BoneCP(config);
		System.out.println("Pre : "+pool.getTotalCreatedConnections());

		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					pool.getConnection();
					System.out.println("In thread: "+pool.getTotalCreatedConnections());

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		
		System.out.println("Post: "+pool.getTotalCreatedConnections());
		Thread.sleep(6000);
		System.out.println("Final: "+pool.getTotalCreatedConnections());
		System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
		Thread.sleep(6000);
		
//		t.start();
//		t.join();
//		t=null;

	}

	
	/**	Test that the values start off at zero initially
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testStatsStartAtZero() throws IllegalArgumentException, IllegalAccessException{
		
		// test that the values start off at zero initially
		checkValuesSetToZero(this.stats);
	}

	/** Main methods.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testStatsFunctionality() throws IllegalArgumentException, IllegalAccessException{
		assertEquals(0, this.stats.getConnectionWaitTimeAvg(), 0.5);
		assertEquals(0, this.stats.getStatementExecuteTimeAvg(), 0.5);
		assertEquals(0, this.stats.getStatementPrepareTimeAvg(), 0.5);
		assertEquals(0, this.stats.getCacheHitRatio(), 0.05);
		
		this.stats.addCumulativeConnectionWaitTime(1000000);
		this.stats.addStatementExecuteTime(1000000);
		this.stats.addStatementPrepareTime(1000000);
		this.stats.incrementCacheHits();
		this.stats.incrementCacheMiss();
		this.stats.incrementConnectionsRequested();
		this.stats.incrementStatementsCached();
		this.stats.incrementStatementsExecuted();
		this.stats.incrementStatementsPrepared();
		
		expect(this.mockPool.getTotalLeased()).andReturn(1).once();
		expect(this.mockPool.getTotalFree()).andReturn(1).once();
		expect(this.mockPool.getTotalCreatedConnections()).andReturn(1).once();
		replay(this.mockPool);
		
		assertEquals(1, this.stats.getCumulativeConnectionWaitTime());
		assertEquals(1, this.stats.getCumulativeStatementPrepareTime());
		assertEquals(1, this.stats.getStatementExecuteTimeAvg(), 0.5);
		assertEquals(1, this.stats.getStatementPrepareTimeAvg(), 0.5);
		assertEquals(1, this.stats.getCumulativeStatementExecutionTime());
		assertEquals(1, this.stats.getConnectionWaitTimeAvg(), 0.5);
		assertEquals(1, this.stats.getStatementsCached());
		assertEquals(1, this.stats.getStatementsExecuted());
		assertEquals(1, this.stats.getStatementsPrepared());
		assertEquals(1, this.stats.getConnectionsRequested());
		assertEquals(1, this.stats.getCacheHits());
		assertEquals(1, this.stats.getCacheMiss());
		assertEquals(1, this.stats.getTotalFree());
		assertEquals(1, this.stats.getTotalCreatedConnections());
		assertEquals(1, this.stats.getTotalLeased());
		assertEquals(0.5, this.stats.getCacheHitRatio(), 0.05);
		
	}
	/**
	 * @param stats
	 * @throws IllegalAccessException
	 */
	private void checkValuesSetToZero(Statistics stats)
			throws IllegalAccessException {
		for (Field field: Statistics.class.getDeclaredFields()){
			if (field.getType().equals(AtomicLong.class) ){
				field.setAccessible(true);
				assertEquals(0, ((AtomicLong)field.get(stats)).get());
			}
			
		}
	}
	
	/** Tests that values are reset properly when instructed to do so.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testStatsReset() throws IllegalArgumentException, IllegalAccessException{
		
		this.stats.resetStats();
		// test that the values start off at zero initially
		checkValuesSetToZero(this.stats);
	}

	
}


