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
/**
 * 
 */
package com.jolbox.bonecp;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Test to see how the pool deals with network/database failures
 * @author wwadge
 *
 */
public class TestRobustness {

	private static MockJDBCDriver driver;

	BoneCPConfig config = new BoneCPConfig();

	@Before
	public void setup() throws SQLException{


		config.setPartitionCount(1);
		config.setMaxConnectionsPerPartition(5);
		config.setMinConnectionsPerPartition(5);
		config.setDisableConnectionTracking(true);
		config.setJdbcUrl("jdbc:mock");
		config.setAcquireRetryAttempts(1);
		config.setAcquireRetryDelay(1, TimeUnit.MILLISECONDS);
	}
	
	
	@After
	public void tearDown() throws SQLException{
		driver.unregister();
	}

	@Test
	public void testDBUnavailableRightAway() throws SQLException{
		driver = new MockJDBCDriver(new MockJDBCAnswer() {

			public Connection answer() throws SQLException {
				throw new SQLException("Unavailable", "8S01");
			}
		});

		try{
			new BoneCP(config);
			fail("Should trigger an exception");
		} catch (SQLException e){
			// nothing
		}
	}
	
	@Test
	public void testFailOnceOnGetConnectionAfterSanityIsOk() throws SQLException{
		final AtomicInteger ai = new AtomicInteger();
		driver = new MockJDBCDriver(new MockJDBCAnswer() {

			public Connection answer() throws SQLException {
				if (ai.getAndIncrement() == 2){ // skip sanity 1st connection, skip 2nd driver force load connection
					throw new SQLException("Unavailable", "8S01");
				}
				return new MockConnection();
			}
		});
			BoneCP pool = new BoneCP(config);
			// we shouldn't blow up - we have other tests to determine coverage/hooks etc
			pool.close();
		}

	@Test
	public void testFailAlwaysOnGetConnectionAfterSanityIsOk() throws SQLException{
		final AtomicInteger ai = new AtomicInteger();
		driver = new MockJDBCDriver(new MockJDBCAnswer() {

			public Connection answer() throws SQLException {
				if (ai.getAndIncrement() >= 2){ // skip sanity 1st connection, skip 2nd driver force load connection
					throw new SQLException("Unavailable", "8S01");
				}
				return new MockConnection();
			}
		});
		
		try{
			new BoneCP(config);
			fail("We should fail the init");
		} catch (SQLException e){
			// nothing
		}
	}
	
	/** Pretend we have a connection that triggers a non-fatal exception (eg duplicate key) 
	 * @throws SQLException */
	@Test
	public void testConnectionTriggersNonFatalException() throws SQLException{
		final Connection mockConnection = createNiceMock(Connection.class);
		driver = new MockJDBCDriver(new MockJDBCAnswer() {


			public Connection answer() throws SQLException {
				return mockConnection;
			}
		});
	
		BoneCP pool = new BoneCP(config);
		expect(mockConnection.prepareStatement((String)anyObject())).andThrow(new SQLException("FOO", "FOO"));
		replay(mockConnection);
		try{
			pool.getConnection().prepareStatement("lalala");
			fail("Should trigger exception");
		} catch (SQLException e){
			assertEquals("FOO", e.getMessage());
		}
		pool.close();
		
	}

	/** Pretend we have a connection that triggers a non-fatal exception (eg duplicate key) 
	 * @throws SQLException */
	@Test
	public void testConnectionDies() throws SQLException{
		final Connection mockConnection = createNiceMock(Connection.class);
		driver = new MockJDBCDriver(new MockJDBCAnswer() {


			public Connection answer() throws SQLException {
				return mockConnection;
			}
		});
	
		BoneCP pool = new BoneCP(config);
		for(ConnectionHandle ch: pool.partitions[0].getFreeConnections()){
			ch.setDebugHandle(123L);
		}
		expect(mockConnection.prepareStatement((String)anyObject())).andThrow(new SQLException("reason", "08S01"));
		replay(mockConnection);
		ConnectionHandle ch = null;
		try{
			ch = (ConnectionHandle) pool.getConnection();
			ch.prepareStatement("lalala");
			fail("Should trigger exception");
		} catch (SQLException e){
			for(ConnectionHandle c: pool.partitions[0].getFreeConnections()){
				assertNotSame(123L,c.getDebugHandle());// all connections should have been killed off
			}
			
				for (int i=0; i < 5; i++){
					if (pool.partitions[0].getFreeConnections().size() == 5){
						break;
					}
					Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
				}
				assertEquals(5, pool.partitions[0].getFreeConnections().size());
			assertTrue(ch.possiblyBroken);
			assertEquals("reason", e.getMessage());
			assertEquals("08S01", e.getSQLState());

		}
		
		ch.close(); // shouldn't fail
		assertTrue(ch.logicallyClosed.get()); // broken so should be marked as such
		pool.close();
	}
}
