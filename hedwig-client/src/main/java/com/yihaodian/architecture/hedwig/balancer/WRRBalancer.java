/**
 * 
 */
package com.yihaodian.architecture.hedwig.balancer;

import java.util.Collection;


import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author root
 *
 */
public class WRRBalancer extends AbstractBalancer {
	private static Logger logger = LoggerFactory.getLogger(WRRBalancer.class);
	@Override
	protected ServiceProfile doSelect() {
		int key = position.getAndIncrement();
		int totalSize = profileCircle.size();
		int realPos = key % totalSize;
		if (key > InternalConstants.INTEGER_BARRIER) {
			position.set(0);
		}
		return getProfileFromCircle(realPos);
	}

	@Override
	public void updateProfiles(Collection<ServiceProfile> serviceSet) {
		lock.lock();
		try {
			if(logger.isDebugEnabled()){
				logger.debug("####----updateProfiles.serviceSet="+ HedwigJsonUtil.toJSONString(serviceSet)+"----####----");
			}
			Circle<Integer, ServiceProfile> circle = new Circle<Integer, ServiceProfile>();
			int size = 0;
			Collection<ServiceProfile> realServiceSet = BalancerUtil.filte(serviceSet, whiteList);
			for (ServiceProfile sp : realServiceSet) {
				int weight = sp.getWeighted();
				for (int i = 0; i < weight; i++) {
					circle.put(size++, sp);
				}
			}
			profileCircle = circle;
		} finally {
			lock.unlock();
		}
	}

}
