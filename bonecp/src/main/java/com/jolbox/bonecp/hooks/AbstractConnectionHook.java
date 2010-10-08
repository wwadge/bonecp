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

package com.jolbox.bonecp.hooks;

import java.sql.Statement;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.PoolUtil;
import com.jolbox.bonecp.StatementHandle;

/** A no-op implementation of the ConnectionHook interface.
 * @author wallacew
 *
 */
public abstract class AbstractConnectionHook implements ConnectionHook {
	/** Class logger. */
	private static final Logger logger = LoggerFactory.getLogger(AbstractConnectionHook.class);

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onAcquire(com.jolbox.bonecp.ConnectionHandle)
	 */
//	@Override
	public void onAcquire(ConnectionHandle connection) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onCheckIn(com.jolbox.bonecp.ConnectionHandle)
	 */
	// @Override
	public void onCheckIn(ConnectionHandle connection) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onCheckOut(com.jolbox.bonecp.ConnectionHandle)
	 */
	// @Override
	public void onCheckOut(ConnectionHandle connection) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onDestroy(com.jolbox.bonecp.ConnectionHandle)
	 */
	// @Override
	public void onDestroy(ConnectionHandle connection) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onAcquireFail(Exception)
	 */ 
	// @Override
	public boolean onAcquireFail(Throwable t, AcquireFailConfig acquireConfig) {
		boolean tryAgain = false;
		String log = acquireConfig.getLogMessage();
		logger.error(log+" Sleeping for "+acquireConfig.getAcquireRetryDelay()+"ms and trying again. Attempts left: "+acquireConfig.getAcquireRetryAttempts()+". Exception: "+t.getCause());

		try {
			Thread.sleep(acquireConfig.getAcquireRetryDelay());
			if (acquireConfig.getAcquireRetryAttempts().get() > 0){
				tryAgain = (acquireConfig.getAcquireRetryAttempts().decrementAndGet()) > 0;
			}
		} catch (Exception e) {
			tryAgain=false;
		}
 
		return tryAgain;  
	}


	public void onQueryExecuteTimeLimitExceeded(ConnectionHandle handle, Statement statement, String sql, Map<Object, Object> logParams){
		onQueryExecuteTimeLimitExceeded(sql, logParams);
	}

	@Deprecated
	public void onQueryExecuteTimeLimitExceeded(String sql, Map<Object, Object> logParams){
		StringBuilder sb = new StringBuilder("Query execute time limit exceeded. Query: ");
		sb.append(PoolUtil.fillLogParams(sql, logParams));
		logger.warn(sb.toString());
	}
	
	public boolean onConnectionException(ConnectionHandle connection, String state, Throwable t) {
		return true; // keep the default behaviour
	}

	@Override
	public void onBeforeStatementExecute(ConnectionHandle conn, StatementHandle statement, String sql, Map<Object, Object> params) {
		// do nothing
	}
	
	@Override
	public void onAfterStatementExecute(ConnectionHandle conn, StatementHandle statement, String sql, Map<Object, Object> params) {
		// do nothing
	}

}