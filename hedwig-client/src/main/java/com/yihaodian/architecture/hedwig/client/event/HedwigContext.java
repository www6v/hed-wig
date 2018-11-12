/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event;

import java.util.Map;

import com.caucho.hessian.client.HessianProxyFactory;
import com.yihaodian.architecture.hedwig.client.locator.IServiceLocator;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.engine.event.IEventContext;

/**
 * @author root
 *
 */
public class HedwigContext implements IEventContext, Cloneable {
	private IServiceLocator<ServiceProfile> locator;
	private Map<String, Object> hessianProxyMap;
	private ClientProfile clientProfile;
	private HessianProxyFactory proxyFactory;
	private Class serviceInterface;

	public IServiceLocator<ServiceProfile> getLocator() {
		return locator;
	}

	public void setLocator(IServiceLocator<ServiceProfile> locator) {
		this.locator = locator;
	}

	public Map<String, Object> getHessianProxyMap() {
		return hessianProxyMap;
	}

	public void setHessianProxyMap(Map<String, Object> hessianProxyMap) {
		this.hessianProxyMap = hessianProxyMap;
	}

	public HedwigContext() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ClientProfile getClientProfile() {
		return clientProfile;
	}

	public void setClientProfile(ClientProfile clientProfile) {
		this.clientProfile = clientProfile;
	}

	public Class getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public HessianProxyFactory getProxyFactory() {
		return proxyFactory;
	}

	public void setProxyFactory(HessianProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}


	public HedwigContext(Map<String, Object> hessianProxyMap, ClientProfile clientProfile, HessianProxyFactory proxyFactory,
			Class serviceInterface) {
		this.hessianProxyMap = hessianProxyMap;
		this.clientProfile = clientProfile;
		this.proxyFactory = proxyFactory;
		this.serviceInterface = serviceInterface;
	}

	@Override
	public HedwigContext clone() throws CloneNotSupportedException {
		HedwigContext context = new HedwigContext(hessianProxyMap, clientProfile, proxyFactory, serviceInterface);
		if (locator != null) {
			context.setLocator(locator);
		}
		return context;
	}

	@Override
	public String toString() {
		return "HedwigContext [locator=" + locator + ", hessianProxyMap=" + hessianProxyMap + ", clientProfile=" + clientProfile
				+ ", proxyFactory=" + proxyFactory + ", serviceInterface=" + serviceInterface + "]";
	}

}
