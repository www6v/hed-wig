package com.yihaodian.architecture.hedwig;

import com.yhd.arch.laserbeak.ReadTask;
import com.yhd.arch.laserbeak.TestBase;
import com.yhd.arch.laserbeak.client.LaserBeakPojoClientFactory;
import com.yhd.arch.photon.constants.PhotonPropKeys;
import com.yhd.arch.spi.IReadService;
import com.yihaodian.architecture.hedwig.client.HedwigClientFactoryBean;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 1/22/16.
 */
public class HedwigClient extends TestBase {

    public void testClient() throws Exception {
        System.setProperty("clientAppName", "root-win");
        int loop = 10;
        ClientProfile profile = new ClientProfile();
        profile.setServiceName("queryService_HESSIAN");
        profile.setDomainName("ARCH-201-SOA");
        profile.setServiceAppName("hedwig");
        profile.setServiceVersion("0.1-wbw");
        profile.setTimeout(500);
        profile.setReadTimeout(300);
        profile.setRedoAble(false);
        // profile.setClientThrottle(false);
        HedwigClientFactoryBean factory = new HedwigClientFactoryBean(com.yhd.arch.spi.IReadService.class,profile);
        final IReadService<String, Integer> readProxy = (IReadService<String, Integer>) factory.getObject();
        final BlockingQueue queue = new LinkedBlockingQueue(10000);
        ThreadPoolExecutor er = new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, queue);

        //Thread.sleep(5000);
        //for(int i=0;i<50;i++){
        //	er.execute(new ReadTask(0, readProxy));
        //}
        System.out.println("warmup end");
        System.out.println("Test start");
        int mod = 3;
        for (long i = 0; i < loop; i++) {
            if (i % mod == 0) {
                try {
                    Thread.sleep(5);
                    System.out.println("sdfasdfasdfasdf");
                    //Thread.currentThread().interrupt();
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
        Thread.sleep(10000000);
    }
}
