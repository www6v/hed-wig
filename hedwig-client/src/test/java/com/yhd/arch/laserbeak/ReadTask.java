package com.yhd.arch.laserbeak;

import com.yhd.arch.spi.IReadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTask implements Runnable {
	static Logger logger = LoggerFactory.getLogger(ReadTask.class);
	private long index;
	private IReadService<String, Integer> readService;

	public ReadTask(long i, IReadService<String, Integer> readService) {
		super();
		this.index = i;
		this.readService = readService;
	}

	@Override
	public void run() {
		try {
			// HedwigContextUtil.setAttribute(PropKeyConstants.PHOTON_CALL_MODEL,
			// Constants.CALL_ONEWAY);
			// System.out.println("queueSize:" + queue.size());
			long start = System.currentTimeMillis();
			String s = readService.readObject(1);
			long cost = System.currentTimeMillis() - start;
			PerformanceProfiler.getInstance().addTotal(cost);
			//System.out.println("request cost:" + cost);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

	}

}
