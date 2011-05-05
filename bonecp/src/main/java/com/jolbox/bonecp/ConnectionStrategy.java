package com.jolbox.bonecp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Marker interface.
 * @author wallacew
 *
 */
public interface ConnectionStrategy {
	
	/** Obtains a connection using the configured strategy. Main entry point. 
	 * @return Connection
	 * @throws SQLException on error
	 */
	Connection getConnection() throws SQLException;
	
	/** Obtains a connection using the configured strategy without blocking.
	 * @return Connection
	 */
	Connection pollConnection();

	/** Destroys all connections using this strategy.
	 */
	void terminateAllConnections();
}
