package com.yhd.arch.zk;

import com.yihaodian.architecture.zkclient.ZkClient;
import junit.framework.TestCase;

import java.util.List;

/**
 * Created by root on 8/4/15.
 */
public class TestConnection extends TestCase{

    public void  testConnection() throws InterruptedException {
        ZkClient zk = new ZkClient("10.161.144.77");
        List<String> l = zk.getChildren("/");
        System.out.print(l);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
