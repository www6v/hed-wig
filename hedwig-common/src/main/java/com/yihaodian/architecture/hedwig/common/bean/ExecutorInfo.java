/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.bean;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author root
 *
 */
public class ExecutorInfo {

	private int poolCoreSize;
	private int poolMaxSize;
	private int poolCurSize;
	/**
	 * active thread in thread pool
	 */
	private int activeThread;
	/**
	 * The largest pool size the pool reached.
	 */
	private int poolLargestSize;
	private int queueCurSize;
	private int queueRemainCapacity;

	public ExecutorInfo(ThreadPoolExecutor executor) {
		poolCurSize = executor.getPoolSize();
		poolCoreSize = executor.getCorePoolSize();
		poolMaxSize = executor.getMaximumPoolSize();
		poolLargestSize = executor.getLargestPoolSize();
		activeThread = executor.getActiveCount();
		queueCurSize = executor.getQueue().size();
		queueRemainCapacity = executor.getQueue().remainingCapacity();
	}

	public int getPoolCoreSize() {
		return poolCoreSize;
	}

	public void setPoolCoreSize(int poolCoreSize) {
		this.poolCoreSize = poolCoreSize;
	}

	public int getPoolMaxSize() {
		return poolMaxSize;
	}

	public void setPoolMaxSize(int poolMaxSize) {
		this.poolMaxSize = poolMaxSize;
	}

	public int getPoolCurSize() {
		return poolCurSize;
	}

	public void setPoolCurSize(int poolCurSize) {
		this.poolCurSize = poolCurSize;
	}

	public int getActiveThread() {
		return activeThread;
	}

	public void setActiveThread(int activeThread) {
		this.activeThread = activeThread;
	}

	public int getPoolLargestSize() {
		return poolLargestSize;
	}

	public void setPoolLargestSize(int poolLargestSize) {
		this.poolLargestSize = poolLargestSize;
	}

	public int getQueueCurSize() {
		return queueCurSize;
	}

	public void setQueueCurSize(int queueCurSize) {
		this.queueCurSize = queueCurSize;
	}

	public int getQueueRemainCapacity() {
		return queueRemainCapacity;
	}

	public void setQueueRemainCapacity(int queueRemainCapacity) {
		this.queueRemainCapacity = queueRemainCapacity;
	}

}
