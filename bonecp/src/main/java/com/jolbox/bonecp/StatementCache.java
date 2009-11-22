package com.jolbox.bonecp;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;


/**
 * JDBC statement cache.
 *
 * @author wallacew
 * @version $Revision$
 */
public class StatementCache implements IStatementCache {

    /** The cache of our statements. */
    private ConcurrentMap<Object, BlockingQueue<Statement>> cache;
    /** Holds a reference just to make our weakhashmap hang on to our objects. */ 
    private BlockingQueue<String> hardCache;
    /** No of entries cached per connection. */
	private int statementsCachedPerConnection;

    /**
     * Creates a statement cache of given size. 
     *
     * @param size of cache.
     * @param statementsCachedPerConnection No of entries cached per connection
     */
    public StatementCache(int size, int statementsCachedPerConnection){
        this.cache = new MapMaker()
        .concurrencyLevel(32)
        .weakKeys()
        .makeMap();
        this.hardCache = new ArrayBlockingQueue<String>(size);
        
        this.statementsCachedPerConnection = Math.max(1, statementsCachedPerConnection);
    }
    /**
     * {@inheritDoc}
     *
     * @see com.jolbox.bonecp.IStatementCache#get(java.lang.String)
     */
    @Override
    public Statement get(String key){
        Statement result = null;
        BlockingQueue<Statement> statementCache = this.cache.get(key);
        
        if (statementCache != null) {
            result = statementCache.poll();
        }
        
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.jolbox.bonecp.IStatementCache#put(java.lang.String, java.sql.Statement)
     */
    @Override
    public void put(String key, Statement value) throws SQLException{
        if (this.hardCache.remainingCapacity() == 0){
            this.hardCache.poll(); // remove the front element
        }
        // might race and fail to insert in cache but this just means 
        // it might not remain cached for long
        this.hardCache.offer(key); 

        // now add to our cache
        BlockingQueue<Statement> queue = new ArrayBlockingQueue<Statement>(this.statementsCachedPerConnection);
        BlockingQueue<Statement> statementCache = this.cache.putIfAbsent(key, queue);
        if (statementCache != null) {
            queue = statementCache;
        }
        
        // save this...
        if (!queue.offer(value)){
            // but if you can't, just throw it away after closing
            ((StatementHandle)value).internalClose(); 
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.jolbox.bonecp.IStatementCache#size()
     */
    @Override
    public int size(){
        return this.cache.size();
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.jolbox.bonecp.IStatementCache#sizeOfQueue(java.lang.String)
     */
    @Override
    public int sizeOfQueue(String sql){
        return this.cache.get(sql).size();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.jolbox.bonecp.IStatementCache#remove(java.lang.String)
     */
    @Override
    public void remove(String sql) {
        this.cache.remove(sql);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.jolbox.bonecp.IStatementCache#clear()
     */
    @Override
    public void clear() {
        this.hardCache.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.jolbox.bonecp.IStatementCache#sizeHardCache()
     */
    @Override
    public int sizeHardCache() {
        return this.hardCache.size();
    }


}
