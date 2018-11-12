package com.yhd.arch.laserbeak;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yihaodian.architecture.test.mockserver.common.IQueryService;
import com.yihaodian.architecture.test.mockserver.common.Result;

/**
 * Created by root on 4/16/15.
 */
public class LaserCompatibleClient {
	private static String envPath = "/Users/root/Applications/work/envConfig";

	public static void main(String[] args) {
		System.setProperty("global.config.path", envPath);
		System.setProperty("clientAppName", "root");
		int loop = 1000;
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "laserCompatible-client.xml" });
		final IQueryService queryService = (IQueryService) context.getBean("readProxy");
		final BlockingQueue queue = new LinkedBlockingDeque<Runnable>();
		ThreadPoolExecutor er = new ThreadPoolExecutor(30, 50, 60, TimeUnit.SECONDS, queue);
		for (long i = 0; i < loop; i++) {
			if (i % 1 == 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				er.execute(new Runnable() {

					@Override
					public void run() {
						// HedwigContextUtil.setAttribute(PropKeyConstants.PHOTON_CALL_MODEL,
						// Constants.CALL_ONEWAY);
						// System.out.println("queueSize:" + queue.size());
						String s = null;
						try {
							long start = System.currentTimeMillis();
							Result result = queryService.queryStrings(1024l * 1000);
							long end = System.currentTimeMillis();
							long cost = end - start;
							// System.out.println(result.getValue());
							System.out.println("cost:" + cost);
							System.out.println();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
