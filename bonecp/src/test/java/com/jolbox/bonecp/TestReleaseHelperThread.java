package com.jolbox.bonecp;


import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

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
	 */
	@Test
	public void testNormalCycle() throws InterruptedException {
		expect(mockQueue.take()).andAnswer(new IAnswer<ConnectionHandle>() {

			@Override
			public ConnectionHandle answer() throws Throwable {
				if (first){
					first = false;
					return mockConnection;
				} 
					throw new InterruptedException();
				
			}
		}).anyTimes();

		mockPool.internalReleaseConnection(mockConnection);
		
		
		replay(mockPool, mockQueue);
		ReleaseHelperThread clazz = new ReleaseHelperThread(mockQueue, mockPool);
		clazz.run();
		verify(mockPool, mockQueue);

		reset(mockPool);
	}
	

}
