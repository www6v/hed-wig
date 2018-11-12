/**
 * 
 */
package com.yihaodian.architecture.hedwig.balancer.special;

import com.yihaodian.architecture.hedwig.balancer.ConditionLoadBalancer;

/**
 * @author root
 * 
 */
public interface DynamicLoadBalancer<P, C> extends ConditionLoadBalancer<P, C> {

	public void setSpecialInfo(ISpecialInfo info);
}
