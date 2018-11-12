/**
 * 
 */
package com.yhd.arch.laserbeak;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author root
 *
 */
public class PerformanceProfiler {

	private static PerformanceProfiler pp = new PerformanceProfiler();

	public static PerformanceProfiler getInstance() {
		return pp;
	}

	private PerformanceProfiler() {
		// TODO Auto-generated constructor stub
	}

	private AtomicLong total = new AtomicLong(0);
	private long avg;
	private AtomicInteger count = new AtomicInteger(0);

	public long getTotal() {
		return total.get();
	}

	public long getAvg() {
		avg = total.get() / count.get();
		total.set(0);
		count.set(0);
		return avg;
	}

	public void addTotal(long cost) {
		total.getAndAdd(cost);
		int i = count.getAndIncrement();
		if (i > 0 && i % 10000 == 0) {
			System.out.println("=======================avg:" + getAvg());
		}
	}

}
