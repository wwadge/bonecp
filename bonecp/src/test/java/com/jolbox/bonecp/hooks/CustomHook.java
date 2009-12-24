package com.jolbox.bonecp.hooks;

import com.jolbox.bonecp.ConnectionHandle;

/** JUnit helper.
 * @author wallacew
 *
 */
public class CustomHook extends AbstractConnectionHook{
    /** junit helper.*/
	int acquire;
	/** junit helper.*/
	int checkin;
	/** junit helper.*/
	int checkout;
	/** junit helper.*/
	int destroy;

	@Override
	public void onAcquire(ConnectionHandle connection) {
		this.acquire++;
	}

	@Override
	public void onCheckIn(ConnectionHandle connection) {
		this.checkin++;
	}

	@Override
	public void onCheckOut(ConnectionHandle connection) {
		this.checkout++;
	}
	
	@Override
	public void onDestroy(ConnectionHandle connection) {
		this.destroy++;
	}

}
