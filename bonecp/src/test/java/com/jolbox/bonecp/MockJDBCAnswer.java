/**
 * 
 */
package com.jolbox.bonecp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Wallace
 *
 */
public interface MockJDBCAnswer {
	Connection answer() throws SQLException;

}
