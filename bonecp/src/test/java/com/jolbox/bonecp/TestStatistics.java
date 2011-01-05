package com.jolbox.bonecp;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
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


