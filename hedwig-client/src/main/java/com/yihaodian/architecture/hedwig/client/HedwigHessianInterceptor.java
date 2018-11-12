/**
 * 
 */
package com.yihaodian.architecture.hedwig.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteAccessor;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import com.yihaodian.architecture.hedwig.client.locator.IServiceLocator;
import com.yihaodian.architecture.hedwig.client.locator.ZkServiceLocator;
import com.yihaodian.architecture.hedwig.common.constants.ServiceStatus;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

/**
 * @author root
 * 
 */
public class HedwigHessianInterceptor extends RemoteAccessor implements MethodInterceptor, InitializingBean {

	private ClientProfile clientProfile;
	private HessianProxyFactory proxyFactory = new HessianProxyFactory();
	private IServiceLocator<ServiceProfile> locator;
	private Map<String, Object> hessianProxyMap = new ConcurrentHashMap<String, Object>();

	@Override
	public Object invoke(MethodInvocation invocation) throws HedwigException {
		Object proxy = null;
		Object result = null;
		if (!HedwigUtil.isBlankString(clientProfile.getTarget())) {
			proxy = getHessianProxy(clientProfile.getTarget());
			result = doInvoke(invocation, proxy, clientProfile.getTarget());
		} else {
			result = doInvoke(invocation);
		}
		return result;
	}

	public Object doInvoke(MethodInvocation invocation) throws HedwigException {
		Object result = null;
		ServiceProfile sp = locator.getService();
		if (sp == null)
			throw new HedwigException("Can't find service provider for :" + clientProfile.toString());
		String sUrl = sp.getServiceUrl();
		Object hessianProxy = getHessianProxy(sUrl);
		if (hessianProxy == null) {
			sp.setCurStatus(ServiceStatus.DISENABLE);
			throw new HedwigException("HedwigHessianInterceptor is not properly initialized");
		}
		ClassLoader originalClassLoader = overrideThreadContextClassLoader();
		try {
			result = invocation.getMethod().invoke(hessianProxy, invocation.getArguments());
		} catch (InvocationTargetException ex) {
			sp.setCurStatus(ServiceStatus.TEMPORARY_DISENABLE);
			if (ex.getTargetException() instanceof HessianRuntimeException) {
				HessianRuntimeException hre = (HessianRuntimeException) ex.getTargetException();
				Throwable rootCause = (hre.getRootCause() != null ? hre.getRootCause() : hre);
				throw new HedwigException(rootCause);
			} else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw new HedwigException(utex.getUndeclaredThrowable());
			}
			throw new HedwigException(ex.getTargetException().getMessage());
		} catch (Throwable ex) {
			sp.setCurStatus(ServiceStatus.TEMPORARY_DISENABLE);
			throw new HedwigException("Failed to invoke Hessian proxy for remote service [" + sUrl + "]", ex);
		} finally {
			resetThreadContextClassLoader(originalClassLoader);
		}
		return result;
	}

	public Object doInvoke(MethodInvocation invocation, Object hessianProxy, String target) throws HedwigException {
		Object result = null;
		ClassLoader originalClassLoader = overrideThreadContextClassLoader();
		try {
			result = invocation.getMethod().invoke(hessianProxy, invocation.getArguments());
		} catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof HessianRuntimeException) {
				HessianRuntimeException hre = (HessianRuntimeException) ex.getTargetException();
				Throwable rootCause = (hre.getRootCause() != null ? hre.getRootCause() : hre);
				throw new HedwigException(rootCause);
			} else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw new HedwigException(utex.getUndeclaredThrowable());
			}
			throw new HedwigException(ex.getTargetException().getMessage());
		} catch (Throwable ex) {
			throw new HedwigException("Failed to invoke Hessian proxy for remote service [" + target + "]", ex);
		} finally {
			resetThreadContextClassLoader(originalClassLoader);
		}
		return result;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!HedwigUtil.isBlankString(clientProfile.getTarget())) {
			createProxy(clientProfile.getTarget());
		} else {
			locator = new ZkServiceLocator(clientProfile);
			Collection<ServiceProfile> serviceProfiles = locator.getAllService();
			for (ServiceProfile profile : serviceProfiles) {
				createProxy(profile.getServiceUrl());
			}
		}
	}

	private Object getHessianProxy(String serviceUrl) throws HedwigException {
		Object proxy = null;
		if (hessianProxyMap.containsKey(serviceUrl)) {
			proxy = hessianProxyMap.get(serviceUrl);
		} else {
			proxy = createProxy(serviceUrl);
		}
		return proxy;
	}

	private Object createProxy(String serviceUrl) throws HedwigException {
		Object proxy = null;
		try {
			proxy = proxyFactory.create(getServiceInterface(), serviceUrl);
			if (proxy != null) {
				hessianProxyMap.put(serviceUrl, proxy);
			}
		} catch (MalformedURLException e) {
			throw new HedwigException(e.getMessage());
		}
		return proxy;
	}

	public void setClientProfile(ClientProfile clientProfile) {
		this.clientProfile = clientProfile;
	}

}
