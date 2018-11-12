package com.yhd.arch.zone;

import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;
import junit.framework.TestCase;

/**
 * Created by root on 8/7/15.
 */
public class ZKUtilTest extends TestCase{

    public void testConn() throws HedwigException {
        String envPath = "/Users/root/Applications/work/envConfig";
        System.setProperty("global.config.path", envPath);
        long start = System.currentTimeMillis();
        ZkClient client = ZkUtil.getZkClientInstance();
        System.out.print(System.currentTimeMillis()-start);
    }

}
