package com.yihaodian.architecture.hedwig.client.event.util;

import com.yihaodian.architecture.hedwig.client.locator.IServiceLocator;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by root on 23/06/2017.
 */
public class Networksitter {

	private static ConcurrentHashMap<String,EventMemo> map = new ConcurrentHashMap<String, EventMemo>();
	public static void listen(ServiceProfile sp,IServiceLocator<ServiceProfile> locator){
		String key = sp.getServiceUrl();
		EventMemo em = map.get(key);
		if(em == null){
			map.put(key,new EventMemo(sp,locator));
		}else{
			em.update();
		}
	}

	//TODO Create a new thread to trigger locator refresh
}
