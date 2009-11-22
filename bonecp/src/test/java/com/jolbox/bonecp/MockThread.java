/**
 * 
 */
package com.jolbox.bonecp;

/**
 * @author Wallace
 *
 */
public class MockThread implements Runnable {

	/** {@inheritDoc}
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(!TestCustomThreadFactory.signalled){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// never reached.
			}
		}
		throw new RuntimeException("fake exception");
	}

}
