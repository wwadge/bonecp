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


import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.easymock.IAnswer;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.ReleaseHelperThread;

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

			@Override
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
		ReleaseHelperThread clazz = new ReleaseHelperThread(mockQueue, mockPool);
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
		ReleaseHelperThread clazz = new ReleaseHelperThread(mockQueue, mockPool);
		clazz.run();
		verify(mockPool, mockQueue);
		reset(mockPool, mockQueue);
		
	
	}
}
