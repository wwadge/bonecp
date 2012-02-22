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


import static org.easymock.EasyMock.*;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.easymock.IAnswer;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.ConnectionReleaseHelperThread;

/**
 * Mock tester for release helper thread
 * @author wwadge
 *
 */
public class TestReleaseHelperThread {
	/** Mock handle. */
	private static BoneCP mockPool;
	/** Mock handle. */
	private static BlockingQueue<ConnectionHandle> mockQueue;
	/** Mock handle. */
	static ConnectionHandle mockConnection;
	/** temp. */
	static boolean first = true;

	/** Mock setup
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setup() throws ClassNotFoundException{
		mockPool = createNiceMock(BoneCP.class);
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockQueue = createNiceMock(BlockingQueue.class);
		
	}
	
	/** Normal case test
	 * @throws InterruptedException
	 * @throws SQLException 
	 */
	@Test
	public void testNormalCycle() throws InterruptedException, SQLException {
		expect(mockQueue.take()).andAnswer(new IAnswer<ConnectionHandle>() {

			// @Override
			public ConnectionHandle answer() throws Throwable {
				if (first){
					first = false;
					return mockConnection;
				} 
					throw new InterruptedException();
				
			}
		}).times(2);

		mockPool.internalReleaseConnection(mockConnection);
		expectLastCall().times(1).andThrow(new SQLException()).once();
		expect(mockQueue.poll()).andReturn(mockConnection).times(2).andReturn(null).once();
		mockPool.poolShuttingDown = true;
			
		
		replay(mockPool, mockQueue);
		ConnectionReleaseHelperThread clazz = new ConnectionReleaseHelperThread(mockQueue, mockPool);
		clazz.run();
		verify(mockPool, mockQueue);
		reset(mockPool, mockQueue);
		
	
	}
	
	/** Normal case test
	 * @throws InterruptedException
	 * @throws SQLException 
	 */
	@Test
	public void testSQLExceptionCycle() throws InterruptedException, SQLException {
		first = true;
		expect(mockQueue.take()).andReturn(mockConnection);
		mockPool.internalReleaseConnection(mockConnection);
		expectLastCall().andThrow(new SQLException());
		
		
		replay(mockPool, mockQueue);
		ConnectionReleaseHelperThread clazz = new ConnectionReleaseHelperThread(mockQueue, mockPool);
		clazz.run();
		verify(mockPool, mockQueue);
		reset(mockPool, mockQueue);
		
	
	}
}
