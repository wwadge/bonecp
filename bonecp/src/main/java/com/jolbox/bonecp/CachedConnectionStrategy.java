package com.jolbox.bonecp;

import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.FinalizableReference;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;

public class CachedConnectionStrategy extends ThreadLocal<ConnectionHandle> {

	/** Logger class. */
	static Logger logger = LoggerFactory.getLogger(CachedConnectionStrategy.class);

	final Map<ConnectionHandle, Reference<Thread>> finalizableRefs = new ConcurrentHashMap<ConnectionHandle, Reference<Thread>>();
	/** Watch for connections that should have been safely closed but the application forgot. */
	private FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
	
	
	private BoneCP pool;

	public CachedConnectionStrategy(BoneCP pool){
		this.pool = pool;
	}

	protected ConnectionHandle initialValue() {
		ConnectionHandle result;
		try {
			// grab a connection like in the default strategy
			result = (ConnectionHandle) this.pool.getConnectionInternal(true);
			// if we were successfull and we're still in caching strategy mode after this operation
			// remember this connection to be able to shutdown cleanly.
			if (result != null && this.pool.cachedPoolStrategy){
				threadWatch(result);
			} else if (this.pool.cachedPoolStrategy){ 
				this.pool.cachedPoolStrategy = false;
				stealExistingAllocations();
				result = (ConnectionHandle) this.pool.getConnectionInternal(false);
			}
		} catch (SQLException e) {
			result = null; 
		}
		return result;
	}



	@Override
	public ConnectionHandle get() {
		ConnectionHandle result = super.get();
		// have we got one that's cached? Mark it as in use. Also check to see if we're still
		// in cached pool strategy mode each time just in case we flipped.
		if (this.pool.cachedPoolStrategy && result != null && !result.inUseInThreadLocalContext.compareAndSet(false, true)){
			try {
				// ... otherwise grab a new connection 
				result = (ConnectionHandle) this.pool.getConnectionInternal(true);
				
			} catch (SQLException e) {
				result = null;
			}
		} 

		if (this.pool.cachedPoolStrategy && result != null){
			// remember this connection to be able to shutdown cleanly.
			threadWatch(result);
		}

		return result;
	}
	protected synchronized void stealExistingAllocations(){
		boolean warnApp = false;
		
		for (ConnectionHandle handle: this.finalizableRefs.keySet()){
			// if they're not in use, pretend they are in use now and close them off.
			// this method assumes that the strategy has been flipped back to non-caching mode
			// prior to this method invocation.
			if (handle.inUseInThreadLocalContext.compareAndSet(false, true)){
				try {
					this.pool.releaseConnection(handle);
					warnApp = true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		if (warnApp){
			logger.warn("Cached strategy chosen, but more threads are requesting a connection than are configured. Switching permanently to default strategy.");
		}
		this.finalizableRefs.clear();
		
	}
	
	private void threadWatch(final ConnectionHandle handle) {
		this.finalizableRefs.put(handle, new FinalizableWeakReference<Thread>(Thread.currentThread(), this.finalizableRefQueue) {
			public void finalizeReferent() {
					try {
						logger.debug("Monitored thread is dead, closing off allocated connection.");
						handle.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					CachedConnectionStrategy.this.finalizableRefs.remove(handle);
			}
		});
	}
	
}
