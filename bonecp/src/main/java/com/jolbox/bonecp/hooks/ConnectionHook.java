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

package com.jolbox.bonecp.hooks;

import com.jolbox.bonecp.ConnectionHandle;

/**
 * Interface to the hooking mechanism of a connection lifecycle. Applications
 * will generally want to extend {@link com.jolbox.bonecp.hooks.AbstractConnectionHook} instead to provide a 
 * default implementation. Applications might also want to make use of 
 * connection.setDebugHandle(...) to keep track of additional information on 
 * each connection.
 * 
 *  Since the class is eventually loaded via reflection, the implementation must 
 *  provide a public, no argument constructor.
 *  
 *  Warning: Be careful to make sure that the hook methods are re-entrant and
 *  thread-safe; do not rely on external state without appropriate locking.
 *   
 * @author wallacew
 * 
 */
public interface ConnectionHook {

	/** Called upon getting a new connection from the JDBC driver (and prior to
	 * inserting into the pool). You may call connection.getRawConnection() to obtain
	 * a handle to the actual (unwrapped) connection obtained from the driver. 
	 * @param connection Handle to the new connection
	 */
	void onAcquire(ConnectionHandle connection); 
	
	/**  Called when the connection is about to be returned to the pool.
	 * @param connection being returned to pool.
	 */
	void onCheckIn(ConnectionHandle connection);

	/**  Called when the connection is extracted from the pool and about to be
	 * given to the application.
	 * @param connection about to given to the app.
	 */
	void onCheckOut(ConnectionHandle connection);
	
	/** Called when the connection is about to be completely removed from the
	 * pool. Careful with this hook; the connection might be marked as being
	 * broken. Use connection.isPossiblyBroken() to determine if the connection
	 * has triggered an exception at some point. 
	 * @param connection
	 */
	void onDestroy(ConnectionHandle connection); 
	
	/** Called on attempting (and failing) to acquire a connection.
	 * @param t Exception that occurred.
	 * @return Return true to attempt the connection again.
	 */
	boolean onAcquireFail(Throwable t);
	 
}
