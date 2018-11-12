/**
 * 
 */
package com.yihaodian.architecture.hedwig.balancer;

import java.util.HashMap;
import java.util.Map;

import com.yihaodian.architecture.hedwig.balancer.special.GrayWRRBalancer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidReturnValueException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

/**
 * @author root
 * 
 */
public class BalancerFactory {

	private static BalancerFactory factory = new BalancerFactory();

	private static Map<String, String> balancerContainer;

	private BalancerFactory() {
		super();
		balancerContainer = new HashMap<String, String>();
		balancerContainer.put(InternalConstants.BALANCER_NAME_ROUNDROBIN, RRBalancer.class.getName());
		balancerContainer.put(InternalConstants.BALANCER_NAME_WEIGHTED_ROUNDROBIN, GrayWRRBalancer.class.getName());
		balancerContainer.put(InternalConstants.BALANCER_NAME_CONSISTENTHASH, ConsistentHashBalancer.class.getName());
		balancerContainer.put(InternalConstants.BALANCER_NAME_WRR_GRAY, GrayWRRBalancer.class.getName());
	}

	public static BalancerFactory getInstance() {
		return factory;
	}

	public LoadBalancer<ServiceProfile> getBalancer(String name) throws HedwigException {
		if (HedwigUtil.isBlankString(name))
			throw new InvalidParamException("Balancer name must not null");
		String clazzName = balancerContainer.get(name);
		if (clazzName != null) {
			try {
				Class clazz = Class.forName(clazzName);
				return (LoadBalancer<ServiceProfile>) clazz.newInstance();
			} catch (Throwable e) {
				throw new InvalidReturnValueException("Can't find " + clazzName + " balancer");
			}
		} else {
			throw new InvalidReturnValueException("Can't find " + name + " balancer");
		}
	}

}
