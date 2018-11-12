package com.yhd.arch.laserbeak;

import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;
import com.yhd.arch.laserbeak.client.LaserBeakPojoClientFactory;
import com.yhd.arch.photon.constants.PhotonPropKeys;
import com.yhd.arch.spi.IReadService;
import com.yhd.arch.zone.ZkClusterUsage;
import com.yhd.arch.zone.ZoneContainer;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.zkclient.ZkClient;
import junit.framework.TestCase;

public class LaserClient extends TestBase{

	/**
	 * -server -Xms512m -Xmx512m -XX:MaxPermSize=64m -XX:+UseConcMarkSweepGC
	 * -XX:+UseParNewGC -XX:+ExplicitGCInvokesConcurrent
	 * -XX:MaxDirectMemorySize=128m -XX:SurvivorRatio=6 -XX:NewRatio=1
	 * 
	 * */
	public void testClient() throws Exception {
		System.setProperty("clientAppName", "root-win");
		//System.setProperty(PhotonPropKeys.KEY_EMITTER_THROTTLER, "com.yhd.arch.photon.plugin.MethodActorAtomicThrottler");
		System.setProperty(PhotonPropKeys.KEY_EMMITTOR_COUNT,"15");
		int loop = 99999999;
		// ApplicationContext context = new ClassPathXmlApplicationContext(new
		// String[] { "laserbeak-client.xml" });
		// final IReadService<String, Integer> readProxy =
		// (IReadService<String,Integer>) context.getBean("readProxy");
		ClientProfile profile = new ClientProfile();
		profile.setServiceName("readService");
		profile.setDomainName("601SOA");
		profile.setServiceAppName("LaserbeakApp");
		profile.setServiceVersion("0.1-jl");
		profile.setTimeout(500);
		profile.setReadTimeout(100);
		profile.setRedoAble(false);
		// profile.setClientThrottle(false);
		LaserBeakPojoClientFactory factory = new LaserBeakPojoClientFactory("readService", profile, "com.yhd.arch.spi.IReadService");
		final IReadService<String, Integer> readProxy = (IReadService<String, Integer>) factory.getObject();
		final BlockingQueue queue = new LinkedBlockingQueue(10000);
		ThreadPoolExecutor er = new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, queue);
		try{
			readProxy.readObject(1);
		}catch (Exception e){
			e.printStackTrace();
		}

		Thread.sleep(5000);
		System.out.println("warmup end");
		System.out.println("Test start");
		int mod = 3;
		for (long i = 0; i < loop; i++) {
			if (i % mod == 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				er.execute(new ReadTask(i, readProxy));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
