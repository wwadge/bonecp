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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

/** For unit testing only
 * @author wwadge
 *
 */

@SuppressWarnings("all")
public class ThreadTester implements Runnable{

	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;
	private long connections;
	private Random rand = new Random();
	private int workDelay;
	private DataSource ds;
	private boolean doPreparedStatement;
	static AtomicInteger c = new AtomicInteger(0);

	public ThreadTester(CountDownLatch startSignal, CountDownLatch doneSignal, DataSource ds, long connections, int workDelay, boolean doPreparedStatement) {
		this.ds=ds;
		this.startSignal = startSignal;
		this.doneSignal = doneSignal;
		this.connections = connections;
		this.workDelay = workDelay;
		this.doPreparedStatement = doPreparedStatement;

	}

	public void run() {
		try {
			this.startSignal.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		for (long i=0; i < this.connections; i++){
			Connection con = null;
			boolean error;
			do {
				try {
					error = false;
					con = this.ds.getConnection();
				} catch (SQLException e) {
					error = true;
					// proxool doesn't wait if it has no free connections, instead it throws an exception 
				}
			} while (error);

			try{
				if (this.doPreparedStatement){
					Statement s = con.prepareStatement(CommonTestUtils.TEST_QUERY);
					s.close();
				}
				if (this.workDelay > 0){
					Thread.sleep(this.workDelay);
				}
				if (this.workDelay < 0){
					Thread.sleep(rand.nextInt(Math.abs(this.workDelay)));
				}
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		this.doneSignal.countDown();

	}

}
