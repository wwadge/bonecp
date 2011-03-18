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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;


/** Tests the statement release helper thread.
 * @author wallacew
 *
 */
public class TestStatementReleaseHelper {

	/** Tests the main functionality
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testThread() throws InterruptedException, SQLException{
		BlockingQueue<StatementHandle> mockQueue = createNiceMock(BlockingQueue.class);
		StatementHandle mockStatement = createNiceMock(StatementHandle.class);
		BoneCP mockPool = createNiceMock(BoneCP.class);
		mockPool.poolShuttingDown = true;
		expect(mockQueue.take()).andReturn(mockStatement).once().andThrow(new NullPointerException()).once().andThrow(new InterruptedException()).once();
		mockStatement.closeStatement();
		expectLastCall().times(2).andThrow(new SQLException()).once();
		
		expect(mockQueue.poll()).andReturn(mockStatement).times(2).andReturn(null).once();
		
		replay(mockQueue, mockStatement);
		StatementReleaseHelperThread testClass = new StatementReleaseHelperThread(mockQueue, mockPool);
		
		
		testClass.run();
		verify(mockQueue, mockStatement);
	}
}
