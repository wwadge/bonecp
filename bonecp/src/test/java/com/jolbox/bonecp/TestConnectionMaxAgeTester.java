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

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Test for connection thread tester
 * @author wwadge
 *
 */
public class TestConnectionMaxAgeTester {
	/** Mock handle. */
	private static BoneCP mockPool;
	/** Mock handle. */
	private static ConnectionPartition mockConnectionPartition;
	/** Mock handle. */
	private static ScheduledExecutorService mockExecutor;
	/** Test class handle. */
	private ConnectionTesterThread testClass;
	/** Mock handle. */
	private static ConnectionHandle mockConnection;
	/** Mock handle. */
	private static BoneCPConfig config;
	/** Mock handle. */
	private static Logger mockLogger;

	/** Mock setup.
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void setup() throws ClassNotFoundException{
		mockPool = createNiceMock(BoneCP.class);
		mockConnectionPartition = createNiceMock(ConnectionPartition.class);
		mockExecutor = createNiceMock(ScheduledExecutorService.class);
		mockConnection = createNiceMock(ConnectionHandle.class);
		mockLogger = createNiceMock(Logger.class);
		
		makeThreadSafe(mockLogger, true);
		config = new BoneCPConfig();
		config.setMaxConnectionAge(1);
	}

	/**
	 * Reset all mocks.
	 */
	@Before
	public void resetMocks(){
		reset(mockPool, mockConnectionPartition, mockExecutor, mockConnection, mockLogger);
	}
	
	
}