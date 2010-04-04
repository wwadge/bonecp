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
package com.jolbox.benchmark;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

/** For unit testing only
 * @author wwadge
 *
 */

@SuppressWarnings("all")
public class ThreadTesterUtil implements Callable<Long>{
	/** A dummy query for HSQLDB. */
	public static final String TEST_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";

	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;
	private Random rand = new Random();
	private int workDelay;
	private DataSource ds;
	private boolean doPreparedStatement;
	static AtomicInteger c = new AtomicInteger(0);

	public ThreadTesterUtil(CountDownLatch startSignal, CountDownLatch doneSignal, DataSource ds, int workDelay, boolean doPreparedStatement) {
		this.ds=ds;
		this.startSignal = startSignal;
		this.doneSignal = doneSignal;
		this.workDelay = workDelay;
		this.doPreparedStatement = doPreparedStatement;

	}

	/** {@inheritDoc}
	 * @see java.util.concurrent.Callable#call()
	 */
	public Long call() throws Exception {
		long time = 0;
		try {
			this.startSignal.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Connection con = null;
		boolean success = false;
		boolean error;
		long start = System.nanoTime();
		do{
			try {
				con = this.ds.getConnection();
				System.out.println(con);
				time = time + (System.nanoTime() - start);
				success = true;
			} catch (Throwable e1) {
				//e1.printStackTrace();
				success = false;
			}
		} while (!success);

		try{
			if (this.doPreparedStatement){
				start = System.nanoTime();
				Statement s = con.prepareStatement(TEST_QUERY);
				s.close();
				time = time + (System.nanoTime() - start);
			}

			if (this.workDelay > 0){
				Thread.sleep(this.workDelay);
			}
			start = System.nanoTime();
			con.close();
			time = time + (System.nanoTime() - start);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable t){
			t.printStackTrace();
		}

		this.doneSignal.countDown();
		return time;
	}

}
