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

/**
 * 
 */
package com.jolbox.bonecp;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.ConnectionPartition;
/**
 * @author wwadge
 *
 */
public class TestConnectionPartition {
	/** mock handle. */
	private BoneCP mockPool;
	/** mock handle. */
	private static BoneCPConfig mockConfig;
	/** mock handle. */
	private static ConnectionPartition testClass;

	
	/**
	 * Tests the constructor. Makes sure release helper threads are launched (+ setup other config items).
	 */
	@Test
	public void testConstructor(){
	    	mockConfig = createNiceMock(BoneCPConfig.class);
	    	expect(mockConfig.getAcquireIncrement()).andReturn(1).anyTimes();
	    	expect(mockConfig.getMaxConnectionsPerPartition()).andReturn(1).anyTimes();
	    	expect(mockConfig.getMinConnectionsPerPartition()).andReturn(1).anyTimes();
	    	expect(mockConfig.getUsername()).andReturn("testuser").anyTimes();
	    	expect(mockConfig.getPassword()).andReturn("testpass").anyTimes();
	    	expect(mockConfig.getJdbcUrl()).andReturn("testurl").anyTimes();
	    	expect(mockConfig.getReleaseHelperThreads()).andReturn(3).anyTimes();
	    	this.mockPool = createNiceMock(BoneCP.class);
	    	expect(this.mockPool.getConfig()).andReturn(mockConfig).anyTimes();
	    	this.mockPool.setReleaseHelper((ExecutorService)anyObject());
	    	expectLastCall().once();
	    	ExecutorService mockReleaseHelper = createNiceMock(ExecutorService.class); 
	    	expect(this.mockPool.getReleaseHelper()).andReturn(mockReleaseHelper).anyTimes();
	    	mockReleaseHelper.execute((Runnable)anyObject());
	    	expectLastCall().times(3);
	    	replay(this.mockPool, mockReleaseHelper, mockConfig);
	    	testClass = new ConnectionPartition(this.mockPool);
	    	verify(this.mockPool, mockReleaseHelper, mockConfig);
	    	reset(this.mockPool, mockReleaseHelper, mockConfig);
	}
	/**
	 * Test method for created connections.
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testUpdateCreatedConnections() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		int count = testClass.getCreatedConnections();
		testClass.updateCreatedConnections(5);
		assertEquals(count+5, testClass.getCreatedConnections());
		
		// Test #2: Same test but fake an exception 
		ReentrantReadWriteLock mockLock = createNiceMock(ReentrantReadWriteLock.class);
		WriteLock mockWriteLock = createNiceMock(WriteLock.class);

		Field field = testClass.getClass().getDeclaredField("statsLock");
		field.setAccessible(true);
		ReentrantReadWriteLock oldLock = (ReentrantReadWriteLock) field.get(testClass);
		field.set(testClass, mockLock);
		expect(mockLock.writeLock()).andThrow(new RuntimeException()).once().andReturn(mockWriteLock).once();
		mockWriteLock.lock();
		expectLastCall().once();
		replay(mockLock, mockWriteLock);
		
		try{
			testClass.updateCreatedConnections(5);
			fail("Should have thrown an exception");
		} catch (Throwable t){
			//do nothing
		}
		verify(mockLock);
		field.set(testClass, oldLock);
		
	}

	/**
	 * Test method for freeConnections
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFreeConnection()  {
		int count = testClass.getCreatedConnections();

		ArrayBlockingQueue<ConnectionHandle> freeConnections = createNiceMock(ArrayBlockingQueue.class);
		testClass.setFreeConnections(freeConnections);
		assertEquals(freeConnections, testClass.getFreeConnections());
		
		ConnectionHandle mockConnectionHandle = createNiceMock(ConnectionHandle.class);
		expect(freeConnections.add(mockConnectionHandle)).andReturn(true);
		expect(freeConnections.remainingCapacity()).andReturn(1).anyTimes();
		replay(mockConnectionHandle, freeConnections);
		testClass.addFreeConnection(mockConnectionHandle);
		verify(mockConnectionHandle, freeConnections);
		assertEquals(count+1, testClass.getCreatedConnections());
		assertEquals(1, testClass.getRemainingCapacity());
		
	}


	/**
	 * Test method for config related stuff.
	 */
	@Test
	public void testConfigStuff() {
		assertEquals("testurl", testClass.getUrl());
		assertEquals("testuser", testClass.getUsername());
		assertEquals("testpass", testClass.getPassword());
		assertEquals(1, testClass.getMaxConnections());
		assertEquals(1, testClass.getMinConnections());
		assertEquals(1, testClass.getAcquireIncrement());
		assertEquals(0, testClass.getConnectionsPendingRelease().size());
	}


	/**
	 * Test method for unable to create more transactions.
	 */
	@Test
	public void testUnableToCreateMoreTransactionsFlag() {
		testClass.setUnableToCreateMoreTransactions(true);
		assertEquals(testClass.isUnableToCreateMoreTransactions(), true);
	}
}
