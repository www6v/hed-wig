package com.yihaodian.architecture.hedwig.common.util;

import com.yhd.arch.TestBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by root on 23/01/2017.
 */
public class TestExecutor extends TestBase{

    ThreadPoolExecutor tpeFront = new ThreadPoolExecutor(10,20,60l, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(1000),new ThreadPoolExecutor.CallerRunsPolicy());
    ThreadPoolExecutor tpeMid = new ThreadPoolExecutor(10,10,60l, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),new ThreadPoolExecutor.CallerRunsPolicy());
    ThreadPoolExecutor tpeEnd = new ThreadPoolExecutor(20,100,60l, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(40));
    Random random = new Random();

    public void testAliveThread(){
        int loopFront = 900000;
        for(int i=0;i<loopFront;i++) {
            tpeFront.execute(new Runnable() {
                public void run() {
                    int loopMid = random.nextInt(10);
                    List<Future<Object>> list = new ArrayList<Future<Object>>();
                    for (int i = 0; i < loopMid; i++) {
                        list.add(tpeMid.submit(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                Future fEnd = tpeEnd.submit(new Callable<Object>() {
                                    @Override
                                    public Object call() throws Exception {
                                        Thread.currentThread().sleep(random.nextInt(10));
                                        return random.nextInt();
                                    }
                                });
                                return fEnd.get(5, TimeUnit.MILLISECONDS);
                            }
                        }));
                    }
                    for (Future fMid : list) {
                        try {
                            System.out.println(fMid.get());
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        } catch (ExecutionException e) {
                            //e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void testFutureGet() throws ExecutionException, InterruptedException {
        Future f = tpeEnd.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                Thread.currentThread().sleep(9000);
                return 666;
            }
        });
        System.out.println(f.get());
    }
}
