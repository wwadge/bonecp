package com.jolbox.bonecp;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createNiceMock;

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
