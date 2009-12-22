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
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.IStatementCache;
import com.jolbox.bonecp.StatementCache;
import com.jolbox.bonecp.StatementHandle;


/** Test class for statement cache
 * @author wwadge
 *
 */
public class TestStatementCache {
	/** Mock handle. */
	private static IStatementCache mockCache;

	/** Mock setup.
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void setup() throws ClassNotFoundException{
		Class.forName("org.hsqldb.jdbcDriver");
		mockCache = createNiceMock(IStatementCache.class);
	}
	/**
	 * Init.
	 */
	@Before
	public void beforeTest(){
		CommonTestUtils.config.setJdbcUrl(CommonTestUtils.url);
		CommonTestUtils.config.setUsername(CommonTestUtils.username);
		CommonTestUtils.config.setPassword(CommonTestUtils.password);
		CommonTestUtils.config.setIdleConnectionTestPeriod(0);
		CommonTestUtils.config.setIdleMaxAge(0);
		CommonTestUtils.config.setPreparedStatementsCacheSize(0);
		CommonTestUtils.config.setReleaseHelperThreads(0);
	}


	
	/** Prepared statement tests.
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testPreparedStatement() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		BoneCP dsb = null ;
		CommonTestUtils.logTestInfo("Tests that prepared statements are obtained from cache when set.");
		CommonTestUtils.config.setMinConnectionsPerPartition(10);
		CommonTestUtils.config.setMaxConnectionsPerPartition(20);
		CommonTestUtils.config.setAcquireIncrement(5);
		CommonTestUtils.config.setPartitionCount(1);
		CommonTestUtils.config.setPreparedStatementsCacheSize(0);
		dsb = new BoneCP(CommonTestUtils.config);

		ConnectionHandle con = (ConnectionHandle) dsb.getConnection();
		Field preparedStatement = con.getClass().getDeclaredField("preparedStatementCache");
		preparedStatement.setAccessible(true);
		// switch to our mock
		preparedStatement.set(con, mockCache);
		expect(mockCache.get(isA(String.class))).andReturn(null);
		mockCache.put(isA(String.class), isA(PreparedStatement.class));

		replay(mockCache);
		Statement statement = con.prepareStatement(CommonTestUtils.TEST_QUERY);
		statement.close();
		verify(mockCache);

		reset(mockCache);
		expect(mockCache.get(isA(String.class))).andReturn(null);
		replay(mockCache);

		con.prepareStatement(CommonTestUtils.TEST_QUERY);
		statement.close();
		verify(mockCache);
		dsb.shutdown();
		statement.close();
		con.close();
		CommonTestUtils.logPass();
	}

	/** Test case for when item is not in cache.
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testStatementCacheNotInCache() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		CommonTestUtils.logTestInfo("Tests statement not in cache.");

		StatementCache cache = new StatementCache(5, 30);
		assertNull(cache.get("nonExistent"));

		CommonTestUtils.logPass();
	}

	/** Test case method for calling different get signatures.
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testStatementCacheDifferentGetSignatures() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		CommonTestUtils.logTestInfo("Tests statement get() signatures.");

		CommonTestUtils.logTestInfo("Tests statement close (put in cache).");
		String sql = CommonTestUtils.TEST_QUERY;
		BoneCP dsb = null ;
		CommonTestUtils.config.setMinConnectionsPerPartition(1);
		CommonTestUtils.config.setMaxConnectionsPerPartition(5);
		CommonTestUtils.config.setAcquireIncrement(1);
		CommonTestUtils.config.setPartitionCount(1);
		CommonTestUtils.config.setPreparedStatementsCacheSize(5);
		dsb = new BoneCP(CommonTestUtils.config);
		Connection conn = dsb.getConnection();
		Statement statement = conn.prepareStatement(sql);
		statement.close();

		
		StatementCache cache = new StatementCache(5, 30);
		cache.put("test1", statement);
		assertNotNull(cache.get("test1"));
		
		assertNull(cache.get("test1", 1));
		assertNull(cache.get("test1", new int[]{1}));
		assertNull(cache.get("test1", new String[]{"1"}));
		assertNull(cache.get("test1", 1, 1));
		assertNull(cache.get("test1", 1, 1, 1));

		CommonTestUtils.logPass();
	}
	
	/** Test case for cache put.
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testStatementCachePut() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		CommonTestUtils.logTestInfo("Tests statement close (put in cache).");
		String sql = CommonTestUtils.TEST_QUERY;
		BoneCP dsb = null ;
		CommonTestUtils.config.setMinConnectionsPerPartition(1);
		CommonTestUtils.config.setMaxConnectionsPerPartition(5);
		CommonTestUtils.config.setAcquireIncrement(1);
		CommonTestUtils.config.setPartitionCount(1);
		CommonTestUtils.config.setPreparedStatementsCacheSize(5);
		dsb = new BoneCP(CommonTestUtils.config);
		Connection conn = dsb.getConnection();
		Statement statement = conn.prepareStatement(sql);
		statement.close();
		Field statementCache = conn.getClass().getDeclaredField("preparedStatementCache");
		statementCache.setAccessible(true);
		IStatementCache cache = (IStatementCache) statementCache.get(conn);
		statement = cache.get(sql);
		assertNotNull(statement);
		// Calling again should not provide the same object
		assertNull(cache.get(sql));

		// now pretend we have 1 connection being asked for the same statement
		// twice
		statement = conn.prepareStatement(sql);
		Statement statement2 = conn.prepareStatement(sql);
		assertEquals(0, cache.sizeOfQueue(sql));

		statement.close(); // release it again
		statement2.close(); // release the other one

		assertEquals(2, cache.sizeOfQueue(sql));
		cache.remove(sql);
		statement2.close();
		statement.close();
		conn.close();
		dsb.shutdown();
		
		
		CommonTestUtils.logPass();
	}

	/** Test limits.
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testStatementCacheLimits() throws SQLException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		CommonTestUtils.logTestInfo("Tests statement caching module.");
		String sql = CommonTestUtils.TEST_QUERY;
		BoneCP dsb = null ;
		CommonTestUtils.config.setMinConnectionsPerPartition(2);
		CommonTestUtils.config.setMaxConnectionsPerPartition(5);
		CommonTestUtils.config.setAcquireIncrement(1);
		CommonTestUtils.config.setPartitionCount(1);
		CommonTestUtils.config.setPreparedStatementsCacheSize(5);
		dsb = new BoneCP(CommonTestUtils.config);
		Connection conn = dsb.getConnection();
		Statement statement = conn.prepareStatement(sql);

		StatementCache cache = new StatementCache(5, 30);
		cache.put("test1", statement);
		cache.put("test2", statement);
		cache.put("test3", statement);
		cache.put("test4", statement);
		cache.put("test5", statement);

		assertEquals(5, cache.sizeHardCache());
		cache.put("test6", statement);
		assertEquals(5, cache.sizeHardCache());

		conn.close();

		// try adding a bunch of statements in the softhashmap, some of them
		// should be garbage collected as it runs out of memory.
		for (int i=0; i < 5000000; i++){
			cache.put("test"+i, statement);
			if ((i % 10000) == 0){
				System.gc();
			}
			if (cache.size() != i) {
				// the garbage collector is doing its thing
				break;
			}
		}
		// some elements should have been dropped in the soft cache
		assertFalse(cache.size()==5000000);
		// but the hardcache should still contain some elements
		assertEquals(5, cache.sizeHardCache());
		// add some more entries...
		String[] testSQL = {"hardcache1", "hardcache2", "hardcache3", "hardcache4", "hardcache5"};
		for (int i=0; i < testSQL.length; i++){
			cache.put(testSQL[i], statement);
		}
		// and test that our cache holds them...
		for (int i=0; i < testSQL.length; i++){
			assertNotNull(cache.get(testSQL[i]));
		}

		dsb.shutdown();
		CommonTestUtils.logPass();
	}

	/** Tests case when statement cache is full.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testStatementCachePutFull() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException{
		BlockingQueue<String> mockHardCache = createNiceMock(BlockingQueue.class);
		ConcurrentMap<Object, BlockingQueue<Statement>> mockLocalCache = createNiceMock(ConcurrentMap.class);
		BlockingQueue<Statement> mockStatementCache = createNiceMock(BlockingQueue.class);
		StatementHandle mockValue = org.easymock.classextension.EasyMock.createNiceMock(StatementHandle.class);
		
		StatementCache testClass = new StatementCache(1, 1);
		Field field = testClass.getClass().getDeclaredField("hardCache");
		field.setAccessible(true);
		field.set(testClass, mockHardCache);

		field = testClass.getClass().getDeclaredField("cache");
		field.setAccessible(true);
		field.set(testClass, mockLocalCache);

		
		expect(mockHardCache.remainingCapacity()).andReturn(1).anyTimes();

		expect(mockHardCache.offer((String)anyObject())).andReturn(true).once();
		
		
		expect(mockLocalCache.putIfAbsent(anyObject(), (BlockingQueue)EasyMock.anyObject())).andReturn(mockStatementCache).once();
		expect(mockStatementCache.offer((Statement)anyObject())).andReturn(false).once();
		mockValue.internalClose();
		
		replay(mockHardCache, mockLocalCache, mockStatementCache);
		org.easymock.classextension.EasyMock.replay(mockValue);
		
		testClass.put("whatever", mockValue);
		verify(mockHardCache, mockLocalCache, mockStatementCache);
		org.easymock.classextension.EasyMock.verify(mockValue);
		
		
	}
	
	/** Tests statement cache clear.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testStatementCacheClear() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		BlockingQueue<String> mockHardCache = createNiceMock(BlockingQueue.class);
		
		StatementCache testClass = new StatementCache(1, 1);
		Field field = testClass.getClass().getDeclaredField("hardCache");
		field.setAccessible(true);
		field.set(testClass, mockHardCache);

		mockHardCache.clear();
		expectLastCall().once();
		replay(mockHardCache);
		testClass.clear();
		verify(mockHardCache);
		
	}
	
}
