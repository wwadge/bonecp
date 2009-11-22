package com.jolbox.bonecp;

import java.sql.SQLException;
import java.sql.Statement;


/**
 * 
 *
 * @author wallacew
 * @version $Revision$
 */
public interface IStatementCache {
    /**
     * Retrieves the cached statement identified by the given key
     *
     * @param key SQL statement
     * @return Statement, or null if not found.
     */
    Statement get(String key);
    
    /**
     * Stores the given Statement in a cache.
     *
     * @param key SQL statement
     * @param value JDBC Statement
     * @throws SQLException on error
     */
    void put(String key, Statement value) throws SQLException;
    
    /**
     * Returns size of the softcache.  
     *
     * @return cache size
     */
    int size();
    /**
     * Returns size of the hard cache.
     *
     * @return hard cache size.
     */
    int sizeHardCache();
    
    /**
     * Returns the size of the queue of identical JDBC statements identified
     * by the given SQL key
     *
     * @param sql
     * @return queue size
     */
    int sizeOfQueue(String sql);
    /**
     * Removes the prepared statement sql cache
     *
     * @param sql statement cache to remove
     */
    void remove(String sql);
    /**
     * Clears the cache
     *
     */
    void clear();
}
