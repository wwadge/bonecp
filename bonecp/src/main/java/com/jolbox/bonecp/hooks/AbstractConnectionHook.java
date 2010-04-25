/**
 * Copyright 2009 Wallace Wadge
 *
 * This file is part of BoneCP.
 *
 * BoneCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BoneCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jolbox.bonecp.hooks;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.ConnectionHandle;

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
	public boolean onAcquireFail(Throwable e) {
		return false; // by default do not try connecting again.
	}

	/** Helper method. FIXME: Move to somewhere common.
	 * @param o items to print
	 * @return String for safe printing.
	 */
	private String safePrint(Object... o){
		StringBuilder sb = new StringBuilder();
		for (Object obj: o){
			sb.append(obj != null ? obj : "null");
		}
		return sb.toString();
	}

	public void onQueryExecuteTimeLimitExceeded(String sql, Map<Object, Object> logParams){
		StringBuilder sb = new StringBuilder("Query execute time limit exceeded. Query: ");
		sb.append(sql+"\n");
		for (Entry<Object, Object> entry: logParams.entrySet()){
			sb.append("[Param #");
			sb.append(entry.getKey());
			sb.append("] ");
			sb.append(safePrint(entry.getValue()));
			sb.append("\n");
		}
		
		if (logger.isWarnEnabled()){
			logger.warn(sb.toString());
		}
	}

}
