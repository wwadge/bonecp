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


import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.reset;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jolbox.bonecp.hooks.AbstractConnectionHook;

/** Tests preparedStatementHandle class.
 * @author wwadge
 *
 */
public class TestPreparedStatementHandle {
	/** Class under test. */
	private PreparedStatementHandle testClass;
	/** Mock class under test. */
	private PreparedStatementHandle mockClass = createNiceMock(PreparedStatementHandle.class);
	/** Mock handles. */
	private IStatementCache mockCallableStatementCache = createNiceMock(IStatementCache.class);
	/** Mock handles. */
	private ConnectionHandle mockConnection = createNiceMock(ConnectionHandle.class);
	/** Mock handles. */
	private BoneCP mockPool = createNiceMock(BoneCP.class);
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		reset(this.mockClass, this.mockCallableStatementCache, this.mockConnection);
		expect(this.mockPool.isStatementReleaseHelperThreadsConfigured()).andReturn(false).anyTimes();
		expect(this.mockConnection.getPool()).andReturn(this.mockPool).anyTimes();
		BoneCPConfig config = new BoneCPConfig();
		expect(this.mockPool.getConfig()).andReturn(config).anyTimes();
		config.setConnectionHook(new AbstractConnectionHook() { 
			// do nothing
		});
		expect(this.mockConnection.isLogStatementsEnabled()).andReturn(true).anyTimes();
		EasyMock.replay(this.mockConnection, this.mockPool);
		this.testClass = new PreparedStatementHandle(this.mockClass, "", this.mockConnection, "TestSQL", this.mockCallableStatementCache);
		EasyMock.reset(this.mockConnection, this.mockPool);
	}

	/** Test that each method will result in an equivalent bounce on the inner statement (+ test exceptions)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@Test
	public void testStandardBounceMethods() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Set<String> skipTests = new HashSet<String>();
		skipTests.add("$VRi"); // this only comes into play when code coverage is started. Eclemma bug?
		CommonTestUtils.testStatementBounceMethod(this.mockConnection, this.testClass, skipTests, this.mockClass);
	}
	
	/**
	 * Coverage.
	 */
	@Test
	public void testGetterSetter(){
		PreparedStatement mockPreparedStatement = createNiceMock(PreparedStatement.class); 
		this.testClass.setInternalPreparedStatement(mockPreparedStatement);
		Assert.assertEquals(mockPreparedStatement, this.testClass.getInternalPreparedStatement());
	}
}