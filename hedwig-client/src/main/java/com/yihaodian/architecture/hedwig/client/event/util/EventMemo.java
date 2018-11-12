package com.yihaodian.architecture.hedwig.client.event.util;

import com.yihaodian.architecture.hedwig.client.event.handle.SyncRequestHandler;
import com.yihaodian.architecture.hedwig.client.locator.IServiceLocator;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by root on 23/06/2017.
 */
public class EventMemo {
	private ServiceProfile sp;
	private IServiceLocator<ServiceProfile> locator;
	private AtomicInteger count;
	private long start;
	private long last;
	private boolean checked=false;

	public EventMemo(ServiceProfile sp,IServiceLocator<ServiceProfile> locator){
		this.sp = sp;
		this.locator = locator;
		count.incrementAndGet();
		start = System.currentTimeMillis();
	}

	public void update(){
		count.incrementAndGet();
		last = System.currentTimeMillis();
	}

	public void checked(){
		//locator.checkServiceProfile(sp);
		checked = true;
	}
}
