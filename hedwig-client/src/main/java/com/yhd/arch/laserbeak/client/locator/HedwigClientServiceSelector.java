package com.yhd.arch.laserbeak.client.locator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.yhd.arch.photon.constants.Constants;
import com.yhd.arch.photon.constants.RemoteServiceType;
import com.yhd.arch.photon.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * This is an utility for switching hedwig services dynamiclly, and the remote
 * service type is config on zk clusters.
 * 
 * @author wangbenwang
 *
 */
public class HedwigClientServiceSelector implements BeanNameAware, FactoryBean, ApplicationContextAware, InvocationHandler {
	private static Logger logger = LoggerFactory.getLogger(HedwigClientServiceSelector.class);
	private String serviceName;
	private ApplicationContext applicationContext;
	private RemoteServiceType remoteServiceType = RemoteServiceType.UNKOWN;
	private ZkClient zkClient;
	private String path; // the path in which hedwig stores remote service type.
	private Object target;
	private Object finalProxy;
	private String interfaceClassName;

	public HedwigClientServiceSelector() {
		initRemoteServiceType();
		watchRemoteServiceType();
	}

	/**
	 * Initialize remote service type from zk.
	 * 
	 * @throws HedwigException
	 */
	private void initRemoteServiceType() {
		try {
			String poolId = YccGlobalPropertyConfigurer.getMainPoolId();
			if (StringUtils.isBlank(poolId)) {
				poolId = ProperitesContainer.client().getProperty("poolId");
				if (StringUtils.isBlank(poolId)) {
					poolId = ProperitesContainer.client().getProperty("poolid");
				}
			}

			if (!com.yihaodian.architecture.hedwig.common.util.StringUtils.isPoolid(poolId)) {
				throw new InvalidParamException("Poolid should match pattern xxxx/xxxx, please config it first.");
			}

			path = new StringBuilder().append(HedwigUtil.genPoolFlagsPath(poolId)).append(Constants.SEPERATOR_SLASH)
					.append(InternalConstants.REMOTE_SERVICE_TYPE).toString();

			zkClient = ZkUtil.getZkClientInstance();

			if (!zkClient.exists(path)) {
				this.remoteServiceType = RemoteServiceType.DEFAULT;
				zkClient.createPersistent(path, true);
				zkClient.writeData(path, this.remoteServiceType.toString());
			} else {
				Object obj = zkClient.readData(path, true);
				if (obj == null) {
					logger.warn(new StringBuilder().append("Empty or null value for the path[").append(path)
							.append("], so default value will be assigned.").toString());
					this.setRemoteServiceType(RemoteServiceType.DEFAULT);
					zkClient.writeData(path, RemoteServiceType.DEFAULT.toString());
				} else {
					this.setRemoteServiceType(RemoteServiceType.getByValue(obj.toString()));
				}
			}
		} catch (Exception e) {
			logger.error("Failed to initialize the remote service type.", e);
		}
	}

	/**
	 * Watch the path in which hedwig stores remote service type information.
	 * 
	 * When the target path is removed, then the watcher on that path will be
	 * removed too.
	 */
	private void watchRemoteServiceType() {
		zkClient.subscribeDataChanges(path, new RemoteServiceTypeListener(this));
	}

	@Override
	public void setBeanName(String name) {
		this.serviceName = name;
	}

	/** 
	 * create a proxy object.
	 * @param target 
	 * @return 
	 */
	public Object bind(Object target) {
		this.target = target;
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}

	/** 
	 * invocation method.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if (target != null) {
			if (logger.isInfoEnabled()) {
				logger.info("The real invoked bean name[" + this.serviceName + "_" + this.remoteServiceType.getValue() + "@"
						+ target.toString() + "]");
			}

		}

		Object result = null;

		/*
		 * Updated by Frank Wang on Dec 26th, 2016
		 * 
		 * By default, JDK dynamic proxy will wrap none runtime exception with UndeclaredThrowableException, 
		 * while leave runtime exception unchanged.
		 * 
		 * So if some functions depend on exception hierarchy, we need some some small surgery here,
		 * so that they could get the real target exception instead of wrapped exception.
		 */
		try {
			result = method.invoke(target, args);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				throw e.getTargetException();
			}
		} catch (UndeclaredThrowableException e) {
			throw new HedwigException(
					"Unexpected exception has been threw, it will impact those functions that depend on exception hierarchy, please contact soa team.",
					e);
		} catch (Throwable ex) {
			throw new HedwigException("Failed to invoke proxy, and the real bean name is: " + this.getRealBeanName() + "]", ex);
		} finally {

		}

		return result;
	}

	/**
	 * Retrieve spring bean instance according to the remote service type
	 * dynamicly.
	 */
	@Override
	public Object getObject() throws Exception {
		String realBeanName = getRealBeanName();

		if (this.applicationContext.containsBean(realBeanName)) {
			if (finalProxy == null) {
				finalProxy = this.bind(this.applicationContext.getBean(realBeanName));
			}

			return finalProxy;
			//return this.bind(this.applicationContext.getBean(realBeanName));
			//return this.applicationContext.getBean(realBeanName);
		} else {
			throw new NoSuchBeanDefinitionException(new StringBuilder().append("Bean[").append(realBeanName)
					.append("] does not exist in spring bean container.").toString());
		}
	}

	/**
	 * Retrieve the spring bean class according to the remote service type
	 * dynamicly.
	 * 
	 * 
	 * FactoryBean threw NullPointerException(warning level) from  this function if it returns null, 
	 * despite the contract saying that it should return null if the type of its object cannot be determined yet.
	 * However, undetermined return object type may cause expected spring annotation behavior, such as @Autowired and so on.
	 * 
	 * Please be aware of this.
	 */
	@Override
	public Class getObjectType() {

		//return null;
		/*
		if (StringUtils.isBlank(this.interfaceClassName)) {
			logger.warn(
					"If you have any issues about AOP compatibility with hedwig when in company compulsive upgrading, please try to upgrade hedwig-0.2.6-SNAPSHOT or later.");
		
			String realBeanName = getRealBeanName();
		
			if (this.applicationContext.containsBean(realBeanName)) {
				return this.applicationContext.getBean(realBeanName).getClass();
			} else {
				logger.error(new StringBuilder().append("Bean[").append(realBeanName)
						.append("] does not exist in spring bean container, so getObjectType() returns null.").toString());
				return null;
			}
		} else {
			Class clazz = null;
			try {
				clazz = Class.forName(this.interfaceClassName);
			} catch (ClassNotFoundException e) {
				logger.error("Invalid interfaface class name: interfaceClassName[" + this.interfaceClassName + "]"
						+ ", and the system will exit.");
				System.exit(-1);
			}
		
			return clazz;
		}
		*/

		if (StringUtils.isBlank(this.interfaceClassName)) {
			logger.warn("Null or empty property of interfaceClassName for the service[" + this.getServiceName()
					+ "] may cause unexpected spring annotaton behavior, such as @Autowired and so on. Please ignore if you don't care.");
			return null;
		} else {
			Class clazz = null;
			try {
				clazz = Class.forName(this.interfaceClassName.trim());
			} catch (ClassNotFoundException e) {
				logger.error("Invalid interfaface class name: interfaceClassName[" + this.interfaceClassName + "]"
						+ ", and the system will exit.");
				System.exit(-1);
			}

			return clazz;
		}

	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Generate the real spring bean name.
	 * 
	 * @return spring bean name.
	 */
	public String getRealBeanName() {
		return new StringBuilder().append(this.serviceName).append(Constants.SEPERATOR_UNDERSCORE).append(this.remoteServiceType.getValue())
				.toString();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public RemoteServiceType getRemoteServiceType() {
		return remoteServiceType;
	}

	public void setRemoteServiceType(RemoteServiceType remoteServiceType) {
		if (remoteServiceType == RemoteServiceType.UNKOWN) {
			logger.warn(new StringBuilder().append("Unkown remote service type, please check the value on the path[").append(path)
					.append("].").toString());
		}
		this.remoteServiceType = remoteServiceType;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	public Object getFinalProxy() {
		return finalProxy;
	}

	public void setFinalProxy(Object finalProxy) {
		this.finalProxy = finalProxy;
	}

	public String getInterfaceClassName() {
		return interfaceClassName;
	}

	public void setInterfaceClassName(String interfaceClassName) {
		this.interfaceClassName = interfaceClassName;
	}

}
