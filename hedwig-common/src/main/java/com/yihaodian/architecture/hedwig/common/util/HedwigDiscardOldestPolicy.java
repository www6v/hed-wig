/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author root
 * 
 */
public class HedwigDiscardOldestPolicy<T> implements RejectedExecutionHandler {

	/*
	 * Remove the head of the Queue then put Runnable into the tail.
	 */
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		if (!executor.isShutdown()) {
			BlockingQueue<Runnable> queue = executor.getQueue();
			try {
				if (queue.size() > 0) {
					Runnable head = queue.poll();
					if (!queue.offer(r)) {
						cancelFuture(r);
					}
					cancelFuture(head);
				}
			} catch (Exception e) {
			}
		}

	}

	public void cancelFuture(Runnable r) {
		if (r instanceof RunnableFuture) {
			RunnableFuture<T> fhead = (RunnableFuture<T>) r;
			fhead.cancel(false);
		}
	}

}
