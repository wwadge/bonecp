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

package com.jolbox.bonecp;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.jolbox.bonecp.hooks.CoverageHook;


/** Tests the functionality of the pool watch thread.
 * @author wwadge
 *
 */
public class TestPoolWatchThread {

	/** Mock handle. */
	private static ConnectionPartition mockPartition;
	/** Mock handle. */
	private static BoneCP mockPool;
	/** Class under test. */
	static PoolWatchThread testClass;
	/** Mock handle. */
	private static Logger mockLogger;
	/** Break out from infinite loop. */
	static boolean first = true;
	/** Mock handle. */
	private static BoneCPConfig mockConfig;

	/** Test class setup.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void setup() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException, ClassNotFoundException{
		Class.forName("org.hsqldb.jdbcDriver");
		mockPartition = createNiceMock(ConnectionPartition.class);


    	mockConfig = createNiceMock(BoneCPConfig.class);
    	expect(mockConfig.getStatementsCacheSize()).andReturn(0).anyTimes();
//    	expect(mockConfig.getConnectionHook()).andReturn(null).anyTimes(); 
    	expect(mockConfig.getAcquireRetryDelay()).andReturn(1000).anyTimes();
		expect(mockConfig.getConnectionHook()).andReturn(new CoverageHook()).anyTimes();
		expect(mockConfig.isLazyInit()).andReturn(true).anyTimes();
		
    	mockPool = createNiceMock(BoneCP.class);
    	expect(mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		replay(mockPool, mockConfig);
		testClass = new PoolWatchThread(mockPartition, mockPool);

		mockLogger = createNiceMock(Logger.class);
		makeThreadSafe(mockLogger, true);

		mockLogger.error((String)anyObject(), anyObject());
		expectLastCall().anyTimes();
		expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();

		mockLogger.debug((String)anyObject(), anyObject());
		expectLastCall().anyTimes();

		Field field = testClass.getClass().getDeclaredField("logger");
		field.setAccessible(true);
		field.set(null, mockLogger);

	}



	/**
	 * Rest the mocks.
	 */
	@Before
	public void doReset(){
		reset(mockPartition, mockPool, mockLogger);
	}

	/** Tests the case where we cannot create more transactions.
	 * @throws InterruptedException
	 */
	@Test
	public void testRunFullConnections() throws InterruptedException{
		mockPartition.lockAlmostFullLock();
		expectLastCall().once();
		mockPartition.unlockAlmostFullLock();
		expectLastCall().anyTimes();

		expect(mockPartition.getMaxConnections()).andReturn(5).once();
		expect(mockPartition.getCreatedConnections()).andReturn(5).once();
		mockPartition.setUnableToCreateMoreTransactions(true);
		expectLastCall().once();
		mockPartition.almostFullWait();
		// just to break out of the loop
		expectLastCall().once().andThrow(new InterruptedException());

		replay(mockPartition, mockPool, mockLogger);
		testClass.run();
		verify(mockPartition);


	}


	/** Tests the normal state.
	 * @throws InterruptedException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testRunCreateConnections() throws InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
		ArrayBlockingQueue<ConnectionHandle> fakeConnections = new ArrayBlockingQueue<ConnectionHandle>(5);
		mockPartition.almostFullWait();
		expectLastCall().anyTimes();
		expect(mockPartition.getMaxConnections()).andAnswer(new IAnswer<Integer>() {

			@Override
			public Integer answer() throws Throwable {
				if (first) {
					first=false;
					return 4;
				} 
				Field field = testClass.getClass().getDeclaredField("signalled");
				field.setAccessible(true);
				field.setBoolean(testClass, true);
				return 4;

			}
		}).anyTimes();
		
		expect(mockPartition.getFreeConnections()).andReturn(fakeConnections).anyTimes();


		expect(mockPartition.getAcquireIncrement()).andReturn(1);
		
		
		expect(mockPartition.getUrl()).andReturn(CommonTestUtils.url).anyTimes();
		expect(mockPartition.getPassword()).andReturn(CommonTestUtils.password).anyTimes();
		expect(mockPartition.getUsername()).andReturn(CommonTestUtils.username).anyTimes();
		mockPartition.addFreeConnection((ConnectionHandle)anyObject());
		expectLastCall().once();
    	expect(mockPool.getConfig()).andReturn(mockConfig).anyTimes();
		replay(mockPool, mockPartition, mockLogger);
		testClass.run();
		verify(mockPartition);
		
		// check exceptional cases
		reset(mockPartition, mockPool, mockLogger);
		resetSignalled();


		first = true;
		mockPartition.unlockAlmostFullLock();
		expectLastCall().once();
		
		mockPartition.lockAlmostFullLock();
		expectLastCall().andThrow(new RuntimeException());
		replay(mockPartition, mockLogger);
		try{
			testClass.run();
			Assert.fail("Exception should have been thrown");
		} catch (RuntimeException e){
			// do nothing
		}
		verify(mockPartition);

		
		// check case where creating new ConnectionHandle fails
		reset(mockPool, mockLogger, mockConfig);
		reset(mockPartition);
		resetSignalled();

		first = true;
		expect(mockPartition.getFreeConnections()).andReturn(fakeConnections).anyTimes();

		expect(mockPartition.getMaxConnections()).andAnswer(new IAnswer<Integer>() {

			@Override
			public Integer answer() throws Throwable {
				if (first) {
					first=false;
					return 4;
				} 
				Field field = testClass.getClass().getDeclaredField("signalled");
				field.setAccessible(true);
				field.setBoolean(testClass, true);
				return 4;

			}
		}).anyTimes();
		
		mockPartition.unlockAlmostFullLock();
		expectLastCall().once();
		
		mockPartition.lockAlmostFullLock();
		expectLastCall().once();

		expect(mockConfig.getStatementsCacheSize()).andAnswer(new IAnswer<Integer>() {
			
			@Override
			public Integer answer() throws Throwable {
				throw new SQLException();
				
			} 
		}).once();
		expect(mockPartition.getAcquireIncrement()).andReturn(1).anyTimes(); 

		mockLogger.error((String)anyObject(), anyObject());
		expectLastCall(); 
		
		replay(mockPartition, mockPool, mockLogger, mockConfig);
		testClass.run();
		verify(mockPartition);

	}


	/**
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private void resetSignalled() throws NoSuchFieldException,
			IllegalAccessException {
		Field field = testClass.getClass().getDeclaredField("signalled");
		field.setAccessible(true);
		field.setBoolean(testClass, false);
	}
}
