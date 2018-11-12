/**
 * 
 */
package com.yihaodian.architecture.hedwig.balancer;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author root
 * 
 */
public class TestNewVersionBalancer extends TestCase {

	Logger logger = LoggerFactory.getLogger(TestNewVersionBalancer.class);
	BlockingQueue<Runnable> bq = new LinkedBlockingQueue<Runnable>();
	ExecutorService es = new ThreadPoolExecutor(10, 20, 10, TimeUnit.MINUTES, bq);
	AtomicInteger ai = new AtomicInteger(0);

	public void testRoundRobin() throws InterruptedException {
		Set<ServiceProfile> serviceSet = new HashSet<ServiceProfile>();
		ServiceProfile sp = null;
		for (int i = 0; i < 2; i++) {
			sp = new ServiceProfile();
			sp.setServiceName("service" + i);
			serviceSet.add(sp);
		}
		final RRBalancer r = new RRBalancer();
		r.updateProfiles(serviceSet);
		for (int m = 0; m < 1000; m++) {
			Thread.sleep(0);
			es.execute(new Runnable() {
				@Override
				public void run() {
					int a = ai.getAndIncrement();
					long start = System.nanoTime();
					r.select();
					System.out.println(Thread.currentThread().getName() + "Compute " + a + " RRBalancer Cost:"
							+ (System.nanoTime() - start) + " blockingQueue size:" + bq.size());

				}
			});
		}
		for (ServiceProfile sp1 : serviceSet) {
			// System.out.println(sp1.getServiceName() + ":" +
			// sp1.getSelectedCount().get());
		}
		es.shutdown();
	}

	public void testWeightedRoundRobin() throws InterruptedException {
		Set<ServiceProfile> serviceSet = new HashSet<ServiceProfile>();
		ServiceProfile sp = null;
		Random random = new Random();
		for (int i = 0; i < 1; i++) {
			sp = new ServiceProfile();
			sp.setServiceName("service" + i);
			sp.setWeighted(random.nextInt(5));
			serviceSet.add(sp);
		}
		final LoadBalancer<ServiceProfile> r = new RRBalancer();
		r.updateProfiles(serviceSet);
		for (int m = 0; m < 1000; m++) {
			Thread.sleep(0);
			es.execute(new Runnable() {
				@Override
				public void run() {
					int a = ai.getAndIncrement();
					long start = System.nanoTime();
					r.select();
					System.out.println(Thread.currentThread().getName() + "Compute " + a + " WRRBalancer Cost:"
							+ (System.nanoTime() - start) + " blockingQueue size:" + bq.size());

				}
			});
		}
		for (ServiceProfile sp1 : serviceSet) {
			// System.out.println(sp1.getServiceName() + ":" +
			// sp1.getSelectedCount().get());
		}
		es.shutdown();
	}

	public void testConsistenthashBalancer() throws InterruptedException {
		Set<ServiceProfile> serviceSet = new HashSet<ServiceProfile>();
		ServiceProfile sp = null;
		Random random = new Random();
		for (int i = 0; i < 1; i++) {
			sp = new ServiceProfile();
			sp.setServiceName("TestService" + i);
			sp.setHostIp("192.168.0." + i);
			sp.setPort(8080);
			sp.setServiceAppName("TestApp");
			sp.setWeighted(random.nextInt(5));
			serviceSet.add(sp);
		}
		final ConditionLoadBalancer<ServiceProfile, String> r = new ConsistentHashBalancer();
		r.updateProfiles(serviceSet);
		for (int m = 0; m < 1000; m++) {
			Thread.sleep(1);
			es.execute(new Runnable() {
				@Override
				public void run() {
					int a = ai.getAndIncrement();
					long start = System.nanoTime();
					r.select();
					System.out.println(Thread.currentThread().getName() + "Compute " + a + " CHBalancer Cost:"
							+ (System.nanoTime() - start) + " blockingQueue size:" + bq.size());

				}
			});
		}
		for (ServiceProfile sp1 : serviceSet) {
			// System.out.println(sp1.getServiceName() + ":" +
			// sp1.getSelectedCount().get());
		}
		es.shutdown();
	}
}
