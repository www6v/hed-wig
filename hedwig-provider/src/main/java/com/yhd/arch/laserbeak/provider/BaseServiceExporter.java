/**
 * 
 */
package com.yhd.arch.laserbeak.provider;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.photon.constants.Constants;
import com.yhd.arch.photon.constants.LaserBeakConstants;
import com.yhd.arch.photon.constants.PhotonPropKeys;
import com.yhd.arch.photon.constants.PhotonPropertiesInjector;
import com.yhd.arch.photon.constants.ProtocolType;
import com.yhd.arch.photon.constants.SupportedCodec;
import com.yhd.arch.photon.entrypoint.EndpointRepository;
import com.yhd.arch.photon.util.ActorNameUtil;
import com.yhd.arch.photon.util.ServerSystem;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.ServiceStatus;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.hedwig.provider.ServiceExporter;
import com.yihaodian.architecture.hedwig.register.IServiceProviderRegister;
import com.yihaodian.architecture.hedwig.register.RegisterFactory;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;
import com.yihaodian.monitor.util.MonitorJmsSendUtil;

/**
 * @author root
 * 
 */
public class BaseServiceExporter implements ServiceExporter {
	private Logger logger = LoggerFactory.getLogger(BaseServiceExporter.class);
	protected IServiceProviderRegister<ServiceProfile> register;
	protected ServiceProfile profile;
	protected AppMeta appMeta;
	protected String serviceName;
	protected Object service;
	protected Class serviceInterface;
	protected String serviceVersion;
	protected String shortServiceName;
	protected boolean initStart = true;
	protected Integer methodWorkerCount = 16;
	protected String user;
	protected String password;
	protected String codecName = SupportedCodec.HEDWIGNESTED;
	protected PhotonPropertiesInjector injector;

	public BaseServiceExporter() {
		super();
	}

	public BaseServiceExporter(ServiceProfile profile, Class serviceInterface) throws Exception {
		super();
		this.profile = profile;
		this.serviceInterface = serviceInterface;
	}

	@Override
	public void destroy() throws Exception {
		register.unRegist(profile);
		MonitorJmsSendUtil.destroy();
		ExporterContainer.getInstance().removeServiceExporter(serviceName, profile.getProfileUUId());
		if (!ExporterContainer.getInstance().hasServiceExporter()) {
			ZkUtil.closeInstance();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		MonitorJmsSendUtil.getInstance();
		register = RegisterFactory.getRegister(LaserBeakConstants.ZONE_REGISTER);

		shortServiceName = HedwigUtil.getRawClassName(service);
		if (profile == null) {
			profile = createServiceProfile();
		}
		profile.setSearalizeTrigger(Constants.SERIALIZE_TRIGGER_CUSTOMIZE);
		profile.setCodecName(codecName);
		profile.setTransProtocol(ProtocolType.AKKAtcp);
		profile.setDomainName(appMeta.getDomainName());
		profile.setServiceAppName(appMeta.getServiceAppName());
		profile.setServiceEnable(initStart);
		if (this.initStart) {
			profile.setCurStatus(ServiceStatus.ENABLE);
		} else {
			profile.setCurStatus(ServiceStatus.DISENABLE);
		}
		profile.setMehodNames(ActorNameUtil.getMethodNames(this.serviceInterface));

		String mainPoolId = YccGlobalPropertyConfigurer.getMainPoolId();
		if (StringUtils.isBlank(mainPoolId)) {
			logger.error("###YccGlobalPropertyConfigurer.getMainPoolId() is NULL!!, getMainPoolId=" + mainPoolId);
		}
		// Add Service publish poolName
		profile.setPubPoolName(mainPoolId);

		int originPort = appMeta.getPort();
		if (originPort < 1800 || originPort > 1999) {
			logger.warn(new StringBuilder()
					.append("According to the convention of yihaodian, the port of listening service for hedwig must be in the range of [1800,1999], otherwise hedwig will use the default port[")
					.append(LaserBeakConstants.DEFAULT_AKKA_PORT).append("] instead.").toString());

			int port = LaserBeakConstants.DEFAULT_AKKA_PORT;
			profile.setPort(port);
		} else {
			profile.setPort(originPort);
		}

		validateProfile();
		ProperitesContainer.provider().HEDWIG_ENV.put(PhotonPropKeys.KEY_AKKAPORT, profile.getPort() + "");
		ServerSystem.getInstance().setWorkActorClass(LogedWorkerActor.class);
		this.user = this.user == null ? appMeta.getUser() : this.user;
		this.password = this.password == null ? appMeta.getPassword() : this.password;
		if (!HedwigUtil.isBlankString(this.user) || !HedwigUtil.isBlankString(this.password)) {
			String sCode = HedwigUtil.genAuthorization(this.user, this.password);
			EndpointRepository.getInstance().registService(serviceName, profile.getProfileUUId(), serviceInterface, service,
					methodWorkerCount, sCode);
		} else {
			EndpointRepository.getInstance().registService(serviceName, profile.getProfileUUId(), serviceInterface, service,
					methodWorkerCount);
		}
		ExporterContainer.getInstance().putServiceExporter(serviceName, profile.getProfileUUId(), this);
		register.regist(profile);
	}

	private ServiceProfile createServiceProfile() throws InvalidParamException {
		if (appMeta == null) {
			throw new InvalidParamException("appContexts must not blank!!!");
		}
		ServiceProfile p = new ServiceProfile();
		if (HedwigUtil.isBlankString(serviceName)) {
			serviceName = serviceInterface.getName();
		}
		p.setServiceName(serviceName);
		if (HedwigUtil.isBlankString(serviceVersion)) {
			throw new InvalidParamException("serviceVersion must not blank!!!");
		}

		if (HedwigUtil.isBlankString(p.getHostIp())) {
			throw new InvalidParamException("Unavailable ip address has been retrieved.");
		}

		p.setServiceVersion(serviceVersion);
		return p;
	}

	private void validateProfile() throws InvalidParamException {
		if (serviceInterface == null) {
			throw new InvalidParamException("Service interface must not be blank!!!");
		}
		if (profile == null) {
			throw new InvalidParamException("Service profile must not be blank!!!");
		}
		if (HedwigUtil.isBlankString(profile.getDomainName())) {
			throw new InvalidParamException("Domain name must not be blank!!!");
		}

		if (profile.getDomainName().contains("${")) {
			throw new InvalidParamException(new StringBuilder().append("DomainName[").append(profile.getDomainName())
					.append("] contains unresolved placeholder, please fix it first.").toString());
		}

		if (HedwigUtil.isBlankString(profile.getServiceAppName())) {
			throw new InvalidParamException("ServiceAppName name must not be blank!!!");
		}

		if (profile.getServiceAppName().contains("${")) {
			throw new RuntimeException(new StringBuilder().append("ServiceAppName[").append(profile.getServiceAppName())
					.append("] contains unresolved placeholder, please fix it first.").toString());
		}

		if (HedwigUtil.isBlankString(profile.getServiceName())) {
			throw new InvalidParamException("Service name must not be blank!!!");
		}

		if (profile.getServiceName().contains("${")) {
			throw new InvalidParamException(new StringBuilder().append("ServiceName[").append(profile.getServiceName())
					.append("] contains unresolved placeholder, please fix it first.").toString());
		}

		if (HedwigUtil.isBlankString(profile.getServiceVersion())) {
			throw new InvalidParamException("Service version must not be blank!!!");
		}

		if (profile.getServiceVersion().contains("${")) {
			throw new InvalidParamException(new StringBuilder().append("ServiceVersion[").append(profile.getServiceVersion())
					.append("] contains unresolved placeholder, please fix it first.").toString());
		}
	}

	@Override
	public void changeServWeight(int newWeight) {
		int weight = profile.getWeighted();
		if (weight != newWeight) {
			try {
				ServiceProfile nsp = profile.clone();
				nsp.increaseVersion();
				nsp.setWeighted(newWeight);
				nsp.setRegistTime(new Date());
				register.updateProfile(nsp);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void changeServEnable(boolean enable) {
		ServiceStatus newStatus = enable ? ServiceStatus.ENABLE : ServiceStatus.DISENABLE;
		if (!profile.getCurStatus().equals(newStatus)) {
			try {
				ServiceProfile nsp = profile.clone();
				nsp.increaseVersion();
				nsp.setCurStatus(newStatus);
				nsp.setRegistTime(new Date());
				register.updateProfile(nsp);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		}
	}

	public void setAppMeta(AppMeta appMeta) {
		this.appMeta = appMeta;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setService(Object service) {
		this.service = service;
	}

	public void setServiceInterface(Class serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public void setShortServiceName(String shortServiceName) {
		this.shortServiceName = shortServiceName;
	}

	public void setInitStart(boolean lazyStart) {
		this.initStart = lazyStart;
	}

	public Class getServiceInterface() {
		return serviceInterface;
	}

	public void setMethodWorkerCount(Integer methodWorkerCount) {
		this.methodWorkerCount = methodWorkerCount;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ServiceProfile getProfile() {
		return profile;
	}

	public void setInjector(PhotonPropertiesInjector injector) {
		if (injector != null) {
			this.injector = injector;
			Map<String, String> map = injector.getSystemProperties();
			ProperitesContainer.provider().pullAll(map);
		}
	}

	public void setCodecName(String codecName) {
		this.codecName = codecName;
	}
}
