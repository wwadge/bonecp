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
package com.jolbox.bonecp.reportedIssues;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.jolbox.bonecp.BoneCPDataSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class BoneCpDataSourceTest {

    @Mock
    private Driver dummyDriver;
    private BoneCPDataSource testDataSource;

    @Before
    public void setUp() throws Exception {
        when(dummyDriver.acceptsURL(anyString())).thenReturn(true);
        when(dummyDriver.connect(anyString(), any(Properties.class))).thenAnswer(new Answer<Connection>() {

        	public Connection answer(InvocationOnMock invocationOnMock) throws Throwable {
      //          Thread.sleep(500);
                System.out.println("Creating dummy connection for: " + invocationOnMock.getArguments()[0]);
                return mock(Connection.class);
            }
        });
        DriverManager.registerDriver(dummyDriver);

        testDataSource = new BoneCPDataSource();
        testDataSource.setJdbcUrl("jdbc://dummy:url");
        testDataSource.setPartitionCount(1);
        testDataSource.setMinConnectionsPerPartition(1);
        testDataSource.setMaxConnectionsPerPartition(6);
        testDataSource.setConnectionTimeoutInMs(5000);
    }

    /**
     * This test fails on multi core machines
     *
     * @throws Exception
     */
    @Test
    public void testConnectionTimeoutOnDataSourceWithOutSync() throws Exception {
        exec(false);
    }

    /**
     * This test passes because of the explicit lock on  testDataSource
     *
     * @throws Exception
     */
    @Test
    public void testConnectionTimeoutOnDataSourceWithSync() throws Exception {
        exec(true);
    }

    private void exec(boolean syncDataSource) throws Exception {
        List<OracleDbThread> dbThreads = new ArrayList<OracleDbThread>();

        int poolMaxCount = testDataSource.getMaxConnectionsPerPartition() * testDataSource.getPartitionCount();
        int connectionsToClaim = poolMaxCount;
        CountDownLatch connectionLatch = new CountDownLatch(connectionsToClaim);
        CountDownLatch executionLatch = new CountDownLatch(connectionsToClaim);
        CountDownLatch releaseLatch = new CountDownLatch(1);

        System.out.println("Max number of connections allowed : " + poolMaxCount);
        System.out.println("Connection timeout in ms : " + testDataSource.getConnectionTimeoutInMs());
        //threads to consume all the connections
        for (int i = 0; i < connectionsToClaim; i++) {
            dbThreads.add(startThread(i, connectionLatch, executionLatch, releaseLatch, syncDataSource));
        }

        //wait for all the connections to be consumed
        try {
            connectionLatch.await();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException caught while waiting for connections");
            throw e;
        }

        //next thread will get a wait for connection timeout
        System.out.println("next thread will get a wait for connection timeout");
        OracleDbThread dbThread = runNoThread(poolMaxCount);

        //release the blocked threads
        System.out.println("release the blocked threads");
        releaseLatch.countDown();

        //wait for the threads to complete
        System.out.println("wait for the threads to complete");
        try {
            executionLatch.await();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException caught while waiting for execute");
            throw e;
        }

        System.out.println("all execution complete");

        for (OracleDbThread oracleDbThread : dbThreads) {
            assertTrue(oracleDbThread.hadConnection.get());
        }

        assertFalse(dbThread.hadConnection.get());
    }

    private OracleDbThread startThread(int threadNum, CountDownLatch connectionLatch, CountDownLatch executionLatch, CountDownLatch releaseLatch, boolean syncDataSource) throws Exception {
        OracleDbThread dbThread = new OracleDbThread(testDataSource, threadNum + 1, connectionLatch, executionLatch, releaseLatch, syncDataSource);
        Thread thread = new Thread(dbThread);
        thread.start();
        return dbThread;
    }

    private OracleDbThread runNoThread(int threadNum) throws Exception {
        OracleDbThread thread = new OracleDbThread(testDataSource, threadNum + 1, null, null, null, false);
        thread.run();
        return thread;
    }

    public class OracleDbThread implements Runnable {
        private final BoneCPDataSource custDataSource;
        private final int threadNum;
        private final AtomicBoolean hadConnection = new AtomicBoolean(false);
        private final CountDownLatch connectionLatch;
        private final CountDownLatch executionLatch;
        private CountDownLatch releaseLatch;
        private boolean syncDataSource;

        public OracleDbThread(BoneCPDataSource custDataSource, int threadNum, CountDownLatch connectionLatch
                , CountDownLatch executionLatch, CountDownLatch releaseLatch, boolean syncDataSource) {
            this.custDataSource = custDataSource;
            this.threadNum = threadNum;
            this.connectionLatch = connectionLatch;
            this.executionLatch = executionLatch;
            this.releaseLatch = releaseLatch;
            this.syncDataSource = syncDataSource;
        }

        public void run() {
            try {

                System.out.println("About to consume connection " + threadNum);
                Connection connection;
                if(syncDataSource) {
                    synchronized (custDataSource) {
                        connection = custDataSource.getConnection();
                    }
                } else {
                    connection = custDataSource.getConnection();
                }
                hadConnection.set(true);

                System.out.println("consume connection OK " + threadNum);
                if (connectionLatch != null) {
                    connectionLatch.countDown();
                }

                if (releaseLatch != null) {
                    System.out.println("about to wait till release " + threadNum);
                    releaseLatch.await();
                    System.out.println("Release " + threadNum);
                }
                connection.close();

                if (executionLatch != null) {
                    executionLatch.countDown();
                }
            } catch (SQLException e) {
                if (connectionLatch != null) {
                    connectionLatch.countDown();
                }
                if (executionLatch != null) {
                    executionLatch.countDown();
                }
                System.out.println(e.getMessage() + " saw an exception in thread " + threadNum);
            } catch (InterruptedException e) {
                if (connectionLatch != null) {
                    connectionLatch.countDown();
                }
                if (executionLatch != null) {
                    executionLatch.countDown();
                }
                System.out.println("thread sleep exception in thread " + threadNum);
                e.printStackTrace();
            }
        }
    }
}
