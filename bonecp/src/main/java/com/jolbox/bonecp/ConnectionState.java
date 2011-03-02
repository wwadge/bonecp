package com.jolbox.bonecp;

/**
 * An enum to indicate the state of a connection to be used as a signal via the onMarkPossiblyBroken
 * hook.
 * 
 * @author wallacew
 *
 */
public enum ConnectionState {
	/** Not sure/don't override default action. */
	NOP,
	/** Test this connection next time it goes back to the pool because it might be broken. */ 
	CONNECTION_POSSIBLY_BROKEN,
	/** Not only is this connection broken but all connections in this pool should be considered
	 * broken too.
	 */
	TERMINATE_ALL_CONNECTIONS;
}
