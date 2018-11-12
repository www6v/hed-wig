package com.yihaodian.architecture.hedwig.common.util;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.util.concurrent.HedwigThreadPoolExecutor;
import junit.framework.TestCase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigThreadPoolExecutorTest extends TestCase {
    public void testThreadLocalCopy001() {
        BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>(20);
        ThreadPoolExecutor tpe = new HedwigThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, eventQueue,
                new HedwigThreadFactory(), new HedwigDiscardOldestPolicy<Object>());
        HedwigContextUtil.setAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "100");
        HedwigContextUtil.setAttribute(InternalConstants.HEDWIG_REQUEST_ID, "111");
        HedwigGlobalIdVo globalIdVo = HedwigContextUtil.getGlobalIdVo();
        System.out.println("0: HEDWIG_TOKEN_GRAY=" + HedwigContextUtil.getAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "null"));
        System.out.println("0: HEDWIG_REQUEST_ID=" + HedwigContextUtil.getAttribute(InternalConstants.HEDWIG_REQUEST_ID, "null"));
        System.out.println("0: HEDWIG_GLOBAL_ID=" + HedwigContextUtil.getGlobalId());
        try {
            tpe.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("2: HEDWIG_TOKEN_GRAY=" + HedwigContextUtil.getAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "null"));
                    System.out.println("2: HEDWIG_REQUEST_ID=" + HedwigContextUtil.getAttribute(InternalConstants.HEDWIG_REQUEST_ID, "null"));
                    System.out.println("2: HEDWIG_GLOBAL_ID=" + HedwigContextUtil.getGlobalId());
                }
            });
        } finally {
            HedwigContextUtil.cleanGlobal(globalIdVo);//清除所有线程变量包括全局与本地
        }
        System.out.println("1: HEDWIG_TOKEN_GRAY=" + HedwigContextUtil.getAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "null"));
        System.out.println("1: HEDWIG_REQUEST_ID=" + HedwigContextUtil.getAttribute(InternalConstants.HEDWIG_REQUEST_ID, "null"));
        System.out.println("1: HEDWIG_GLOBAL_ID=" + HedwigContextUtil.getGlobalId());
//        try {
//            Thread.sleep(50000L);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void testThreadLocalCopy002() {
        BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>(20);
        ThreadPoolExecutor tpe = new HedwigThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, eventQueue,
                new HedwigThreadFactory(), new HedwigDiscardOldestPolicy<Object>());
        HedwigContextUtil.setAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "100");
        HedwigContextUtil.setAttribute(InternalConstants.HEDWIG_REQUEST_ID, "111");
        System.out.println("0: HEDWIG_TOKEN_GRAY=" + HedwigContextUtil.getAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "null"));
        System.out.println("0: HEDWIG_REQUEST_ID=" + HedwigContextUtil.getAttribute(InternalConstants.HEDWIG_REQUEST_ID, "null"));
        HedwigContextUtil.getGlobalId();
        HedwigGlobalIdVo globalIdVo = HedwigContextUtil.getGlobalIdVo();
        try {
            tpe.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("2: HEDWIG_TOKEN_GRAY=" + HedwigContextUtil.getAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "null"));
                    System.out.println("2: HEDWIG_REQUEST_ID=" + HedwigContextUtil.getAttribute(InternalConstants.HEDWIG_REQUEST_ID, "null"));
                    System.out.println("2: HEDWIG_GLOBAL_ID=" + HedwigContextUtil.getGlobalId());
                }
            });
        } finally {
            HedwigContextUtil.cleanGlobal(globalIdVo);//清除所有线程变量包括全局与本地
        }
        System.out.println("1: HEDWIG_TOKEN_GRAY=" + HedwigContextUtil.getAttribute(PropKeyConstants.HEDWIG_TOKEN_GRAY, "null"));
        System.out.println("1: HEDWIG_REQUEST_ID=" + HedwigContextUtil.getAttribute(InternalConstants.HEDWIG_REQUEST_ID, "null"));
        System.out.println("1: HEDWIG_GLOBAL_ID=" + HedwigContextUtil.getGlobalId());
//        try {
//            Thread.sleep(50000L);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
