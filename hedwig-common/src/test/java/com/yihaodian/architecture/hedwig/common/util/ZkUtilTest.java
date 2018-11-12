package com.yihaodian.architecture.hedwig.common.util;


import com.yihaodian.architecture.zkclient.ZkClient;
import junit.framework.TestCase;

public class ZkUtilTest extends TestCase {
    public void testZkDoCopy() {
        try {
            ZkClient zk = ZkUtil.getZkClientInstance();
            String fromPath = "/ZoneMeta";
            String toPath = "/ZoneMeta-CopyTest";
            if(zk.exists(toPath)){
                zk.delete(toPath);
            }
            ZkUtil.doCopy(zk, fromPath, zk, toPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
