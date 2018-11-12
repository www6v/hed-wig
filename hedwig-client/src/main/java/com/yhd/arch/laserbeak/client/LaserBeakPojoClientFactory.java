package com.yhd.arch.laserbeak.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.yhd.arch.container.RootContainer;
import com.yhd.arch.laserbeak.client.locator.ServiceKeeper;
import com.yhd.arch.laserbeak.client.meta.HedwigClientConfigLoader;
import com.yhd.arch.laserbeak.client.meta.HedwigClientConfigUtil;
import com.yhd.arch.photon.constants.PhotonPropertiesInjector;
import com.yhd.arch.photon.core.RemoteMetaData;
import com.yhd.arch.photon.core.ServiceInfo;
import com.yhd.arch.photon.util.ClientSystem;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.jmx.AbstractJMXRegistration;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.monitor.util.MonitorJmsSendUtil;

public class LaserBeakPojoClientFactory implements FactoryBean, InitializingBean, DisposableBean {
	protected ClientProfile clientProfile = new ClientProfile();
	private Object serviceProxy;
	private String serviceAppName;
	private String domainName;
	private String serviceName;
	private String serviceVersion;
	private String target;
	private String clientAppName;
	private String clientVersion;
	private String groupName;
	private long readTimeout = ProperitesContainer.client().getLongProperty(PropKeyConstants.HEDWIG_READ_TIMEOUT,
			InternalConstants.DEFAULT_READ_TIMEOUT);
	private long timeout;
	private boolean autoRedo = false;
	private Set<String> noRetryMethods;
	private boolean clientThrottle = ProperitesContainer.client().getBoolean(PropKeyConstants.HEDWIG_CLIENT_THROTTLE, true);
	private String serviceInterface;
	private Class objType;
	protected String user;
	protected String password;
	private int senderCount = 20;
	private boolean useBroadCast = false;
	private String balanceAlgo = InternalConstants.BALANCER_NAME_WEIGHTED_ROUNDROBIN;
	private ServiceKeeper serviceKeeper;
	private PhotonPropertiesInjector injector;

	private String clientPoolId;// 客户端配置中心中的poolId（新增）（如果没有默认读取配置中心里面的当前poolId）开发环境下不同配置，通过不同后缀表示
	private String providerPoolId;// 服务提供方配置中心中的poolId（新增）
	private IRequestListener listener;

	public LaserBeakPojoClientFactory() {
		super();
	}

	public LaserBeakPojoClientFactory(String serviceName, ClientProfile clientProfile, String serviceInterface) throws Exception {
		super();
		this.clientProfile = clientProfile;
		this.serviceInterface = serviceInterface;
		this.serviceName = serviceName;
		afterPropertiesSet();
	}

	public LaserBeakPojoClientFactory(String serviceName, ClientProfile clientProfile, String serviceInterface, IRequestListener listener)
			throws Exception {
		super();
		this.clientProfile = clientProfile;
		this.serviceInterface = serviceInterface;
		this.serviceName = serviceName;
		this.listener = listener;
		afterPropertiesSet();
	}

	@Override
	public void destroy() throws Exception {
		serviceKeeper.destory();
		MonitorJmsSendUtil.destroy();
		ZkUtil.closeInstance();
	}

	@Override
	public void afterPropertiesSet() throws Exception {    
		setServiceNameInThreadLocal();
		
		HedwigClientUtil.getAppPathDict();
		MonitorJmsSendUtil.getInstance();
		objType = Class.forName(this.serviceInterface);
		clientProfile.setServiceInterface(objType);
		clientProfile.setSenderCount(senderCount);
		// 加载远程服务配置到内存
		HedwigClientConfigLoader configLoader = HedwigClientConfigLoader.getLoader(clientPoolId, providerPoolId);
		if (configLoader != null) {
			// 加载远程配置
			boolean loadedSuccess = configLoader.load();
			if (loadedSuccess) {
				// 更新并覆盖本地配置
				clientProfile.setClientPoolId(configLoader.getClientPoolId());
				clientProfile.setProviderPoolId(configLoader.getProviderPoolId());
				clientProfile = HedwigClientConfigUtil.updateClientProfile(serviceName, clientProfile);
			}
		}
		validate(clientProfile);
		ClientSystem.getInstance().setAckActorCLass(LogedAckActor.class);
		RootContainer.getInstance().putClientProfile(serviceName, clientProfile);
		RemoteMetaData meta = ClientFactoryHelper.createMeta(clientProfile);
		ServiceInfo info = new ServiceInfo(meta);
		serviceKeeper = ClientFactoryHelper.createServiceLookuper(clientProfile, info);
		InvocationHandler handler = null;
		if (this.listener != null) {
			handler = new RequestHandler(clientProfile, meta, info, this.listener);
		} else {
			handler = new RequestHandler(clientProfile, meta, info);
		}

		this.serviceProxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { objType }, handler);
		
		removeServiceNameInThreadLocal();
	}

	private void setServiceNameInThreadLocal() {
		AbstractJMXRegistration.serviceNameLocal.set(serviceName);
	}
	
	private void removeServiceNameInThreadLocal() {
		AbstractJMXRegistration.serviceNameLocal.remove();
	}

	@Override
	public Object getObject() throws Exception {
		return serviceProxy;
	}

	@Override
	public Class getObjectType() {
		return objType;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	private void validate(ClientProfile clientProfile) throws InvalidParamException {
		if (HedwigUtil.isBlankString(clientProfile.getClientAppName())) {
			throw new InvalidParamException("clientAppName must not blank!!!");
		}
		if (HedwigUtil.isBlankString(target)) {
			if (HedwigUtil.isBlankString(clientProfile.getDomainName())
					|| InternalConstants.UNKONW_DOMAIN.equals(clientProfile.getDomainName())) {
				throw new InvalidParamException("domainName must not blank!!!");
			}
			if (HedwigUtil.isBlankString(clientProfile.getServiceAppName())
					|| "defaultLaserAppName".equals(clientProfile.getServiceAppName())) {
				throw new InvalidParamException("serviceAppName must not blank!!!");
			}
			if (HedwigUtil.isBlankString(clientProfile.getServiceVersion())
					|| "defaultLaserVersion".equals(clientProfile.getServiceVersion())) {
				throw new InvalidParamException("serviceVersion must not blank!!!");
			}
			if (HedwigUtil.isBlankString(clientProfile.getServiceName())
					|| "defaultLaserServiceName".equals(clientProfile.getServiceName())) {
				throw new InvalidParamException("serviceName must not blank!!!");
			}
		}
	}

	public void setServiceAppName(String serviceAppName) {
		clientProfile.setServiceAppName(serviceAppName);
		this.serviceAppName = clientProfile.getServiceAppName();
	}

	public void setServiceName(String serviceName) {
		clientProfile.setServiceName(serviceName);
		this.serviceName = clientProfile.getServiceName();
	}

	public void setServiceVersion(String serviceVersion) {
		clientProfile.setServiceVersion(serviceVersion);
		this.serviceVersion = clientProfile.getServiceVersion();
	}

	public void setTarget(String target) {
		this.target = target;
		clientProfile.setTarget(target);
	}

	public void setTimeout(Long timeout) {
		clientProfile.setTimeout(timeout);
		this.timeout = clientProfile.getTimeout();
	}

	public void setDomainName(String domainName) {
		clientProfile.setDomainName(domainName);
		this.domainName = clientProfile.getDomainName();
	}

	/**
	 * Get from configure center, You need this method only in unit test.
	 * 
	 * @param clientAppName
	 */
	@Deprecated
	public void setClientAppName(String clientAppName) {
		clientProfile.setClientAppName(clientAppName);
		this.clientAppName = clientProfile.getClientAppName();
	}

	public void setNoRetryMethods(Set<String> noRetryMethods) {
		clientProfile.setNoRetryMethods(noRetryMethods);
		this.noRetryMethods = noRetryMethods;

	}

	public void setGroupName(String groupName) {
		clientProfile.setStrGroupName(groupName);
		this.groupName = groupName;
	}

	public void setAutoRedo(boolean autoRedo) {
		clientProfile.setRedoAble(autoRedo);
		this.autoRedo = clientProfile.isRedoAble();
	}

	public void setClientVersion(String clientVersion) {
		clientProfile.setClientVersion(clientVersion);
		this.clientVersion = clientProfile.getClientVersion();
	}

	public void setClientThrottle(boolean clientThrottle) {
		clientProfile.setClientThrottle(clientThrottle);
		this.clientThrottle = clientProfile.isClientThrottle();
	}

	public void setUseBroadCast(boolean useBroadCast) {
		clientProfile.setUseBroadcast(useBroadCast);
		this.useBroadCast = useBroadCast;
	}

	public void setUser(String user) {
		this.clientProfile.setUser(user);
		this.user = user;
	}

	public void setPassword(String password) {
		this.clientProfile.setPassword(password);
		this.password = password;
	}

	public void setReadTimeout(long readTimeout) {
		this.clientProfile.setReadTimeout(readTimeout);
		this.readTimeout = clientProfile.getReadTimeout();
	}

	public void setBalanceAlgo(String balanceAlgo) {
		this.clientProfile.setBalanceAlgo(balanceAlgo);
		this.balanceAlgo = clientProfile.getBalanceAlgo();
	}

	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public void setSenderCount(int senderCount) {
		this.clientProfile.setSenderCount(senderCount);
		this.senderCount = this.clientProfile.getSenderCount();
	}

	public void setInjector(PhotonPropertiesInjector injector) {
		if (injector != null) {
			this.injector = injector;
			Map<String, String> map = injector.getSystemProperties();
			ProperitesContainer.client().pullAll(map);
		}
	}

	public void setListener(IRequestListener listener) {
		this.listener = listener;
	}

	public String getProviderPoolId() {
		return providerPoolId;
	}

	public void setProviderPoolId(String providerPoolId) {
		this.providerPoolId = providerPoolId;
	}

	public String getClientPoolId() {
		return clientPoolId;
	}

	public void setClientPoolId(String clientPoolId) {
		this.clientPoolId = clientPoolId;
	}
}
