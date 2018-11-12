package com.yhd.arch.laserbeak.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import com.yhd.arch.spi.IReadService;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;

public class QueryService implements IReadService<String, String> {

	private AtomicInteger count = new AtomicInteger(0);
	private long start = System.currentTimeMillis();
	private int step = 100000;
	private long st = System.currentTimeMillis();

	@Override
	public String readObject(String p) {
		count.incrementAndGet();
		if (count.get() % step == 0) {
			long et = System.currentTimeMillis();
			System.out.println(count.get());
			double tps = step * 1000 / (et - st);
			System.out.println("TPS:" + tps);
			st = System.currentTimeMillis();
			System.out.println(HedwigContextUtil.getAttribute("aaaaaaa", "unKnown"));
		}
		long now = System.currentTimeMillis();
		if (now - start > 30000) {
			count.set(0);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		start = now;
		return count.get() + "";
	}

	@Override
	public Collection<String> readCollection(String p, Integer size) {
		ArrayList<String> l = new ArrayList<String>();
		l.add(count.get() + "");
		return l;
	}

}
