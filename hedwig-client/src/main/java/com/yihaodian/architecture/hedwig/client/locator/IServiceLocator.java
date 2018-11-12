/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.locator;

import com.yihaodian.architecture.hedwig.balancer.LoadBalancer;

import java.util.Collection;

/**
 * Use to get all the service provider node from register center. 
 * This class should be node quantity sensitive.
 * When add/remove node locator should notify interesting component.
 * 
 * @author root
 * 
 */
public interface IServiceLocator<E> {

	public E getService();

	public Collection<E> getAllService();

	public LoadBalancer<E> getLoadBalancer();

}
