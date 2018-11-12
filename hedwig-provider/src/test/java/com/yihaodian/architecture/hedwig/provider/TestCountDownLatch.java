package com.yihaodian.architecture.hedwig.provider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

public class TestCountDownLatch extends TestCase{

	ExecutorService es = Executors.newFixedThreadPool(20);
	
	public void testLatch() throws InterruptedException{
		final CountDownLatch cdl = new CountDownLatch(10);
		for(int i=0;i<9;i++){
			es.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						cdl.await();
						System.out.println("do smothing");
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});
			cdl.countDown();
		}
	}
	
	public void testCycliBarrier(){
		CyclicBarrier cb = new CyclicBarrier(10);
	}
}
