/**
 * 
 */
package com.jolbox.bonecp.hooks;

import com.jolbox.bonecp.ConnectionHandle;

/** A no-op implementation of the ConnectionHook interface.
 * @author wallacew
 *
 */
public abstract class AbstractConnectionHook implements ConnectionHook {

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onAcquire(com.jolbox.bonecp.ConnectionHandle)
	 */
	@Override
	public void onAcquire(ConnectionHandle connection) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onCheckIn(com.jolbox.bonecp.ConnectionHandle)
	 */
	@Override
	public void onCheckIn(ConnectionHandle connection) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onCheckOut(com.jolbox.bonecp.ConnectionHandle)
	 */
	@Override
	public void onCheckOut(ConnectionHandle connection) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.jolbox.bonecp.hooks.ConnectionHook#onDestroy(com.jolbox.bonecp.ConnectionHandle)
	 */
	@Override
	public void onDestroy(ConnectionHandle connection) {
		// do nothing
	}

}
