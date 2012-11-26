/**
 * 
 */
package com.jolbox.bonecp;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

/** Tests for AbstractConnectionStrategy
 * @author wwadge
 *
 */
public class TestAbstractConnectionStrategy {

	@Test
	public void test() {
		@SuppressWarnings("serial")
		class X extends AbstractConnectionStrategy{
				public void terminateAllConnections() {
				}

				protected Connection getConnectionInternal() throws SQLException {
					return null;
				}
			}
		
		assertNull(new X().pollConnection());
	}

}
