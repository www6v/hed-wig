/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import java.util.concurrent.ThreadFactory;

/**
 * @author root
 *
 */
public class HedwigThreadFactory implements ThreadFactory {


	/* (non-Javadoc)
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, "Hedwig worker thread");
		t.setDaemon(true);
		t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

}
