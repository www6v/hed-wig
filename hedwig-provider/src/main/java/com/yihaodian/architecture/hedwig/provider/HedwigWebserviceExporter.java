/**
 * 
 */
package com.yihaodian.architecture.hedwig.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.web.HttpRequestHandler;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.ServiceStatus;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.hedwig.common.uuid.MD5;
import com.yihaodian.architecture.hedwig.hessian.HedwigHessianExporter;
import com.yihaodian.architecture.hedwig.register.IServiceProviderRegister;
import com.yihaodian.architecture.hedwig.register.RegisterFactory;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;
import com.yihaodian.monitor.dto.ServerBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import com.yihaodian.monitor.util.MonitorJmsSendUtil;

/**
 * @author root
 * 
 */
public class HedwigWebserviceExporter extends HedwigHessianExporter implements HttpRequestHandler, ServiceExporter, BeanNameAware {

	private Logger logger = LoggerFactory.getLogger(HedwigWebserviceExporter.class);
	private IServiceProviderRegister register;
	private ServiceProfile profile;
	private AppProfile appProfile;
	private String serviceName;
	private String serviceVersion;
	private boolean defaultStatus = true;
	private String shortServiceName;
	private String poolName = YccGlobalPropertyConfigurer.getMainPoolId();

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!postCheck(request, response))
			return;
		ServerBizLog sbLog = new ServerBizLog();
		try {
			Date start = new Date();
			sbLog.setGetReqTime(start);
			HedwigContextUtil.setAttribute(InternalConstants.HEDWIG_MONITORLOG, sbLog);
			sbLog.setProviderApp(poolName);
			sbLog.setProviderHost(profile.getHostIp() + ":" + profile.getPort());
			sbLog.setServiceName(profile.getServiceName());
			
			invoke(request.getInputStream(), response.getOutputStream());
			
			sbLog.setSuccessed(MonitorConstants.SUCCESS);
		} catch (Throwable ex) {
			sbLog.setInParamObjects(HedwigContextUtil.getArguments());
			sbLog.setSuccessed(MonitorConstants.FAIL);
			sbLog.setExceptionClassname(HedwigMonitorUtil.getExceptionClassName(ex));
			sbLog.setExceptionDesc(HedwigMonitorUtil.getExceptionMsg(ex));
			logger.error(ex.getMessage(), ex);
		} finally {
			sbLog.setProviderZone(profile.getPubZone());
			sbLog.setProviderIDC(profile.getPubIdc());
			sbLog.setProviderLevel(profile.getProviderLevel());

			sbLog.setMethodName(shortServiceName + "." + sbLog.getMethodName());
			sbLog.setUniqReqId(HedwigContextUtil.getGlobalId());
			sbLog.setServiceVersion(profile.getServiceVersion());
			MonitorJmsSendUtil.asyncSendServerBizLog(sbLog);
			HedwigContextUtil.cleanGlobal();
		}
	}

	protected boolean postCheck(HttpServletRequest request, HttpServletResponse response) {
		boolean value = true;
		try {
			if (!"POST".equals(request.getMethod())) {
				value = false;
				String errMsg = "Hedwig requires POST!!!";
				response.setStatus(500, errMsg);
				PrintWriter out = response.getWriter();
				response.setContentType("text/html");
				out.println("<h1>" + errMsg + "</h1>");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public void destroy() throws Exception {
		register.unRegist(profile);
		logger.info("HedwigExporter destory service, serviceName:" + profile.getServiceName() + ",url:" + profile.getServiceUrl());
		ZkUtil.getZkClientInstance().unsubscribeAll();
		MonitorJmsSendUtil.destroy();
		ZkUtil.closeInstance();
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		MonitorJmsSendUtil.getInstance();
		checkAppProfile(appProfile);
		try {
			shortServiceName = HedwigUtil.getRawClassName(getService());
			if (profile == null) {
				profile = createServiceProfile();
			}
			profile.setDomainName(appProfile.getDomainName());
			profile.setServiceAppName(appProfile.getServiceAppName());
			profile.setUrlPattern(appProfile.getUrlPattern());
			profile.setAssembleAppName(appProfile.isAssembleAppName());
			String mainPoolId = YccGlobalPropertyConfigurer.getMainPoolId();
			if (StringUtils.isBlank(mainPoolId)) {
				logger.error("###YccGlobalPropertyConfigurer.getMainPoolId() is NULL!!, getMainPoolId=" + mainPoolId);
			}
			// Add Service publish poolName
			profile.setPubPoolName(mainPoolId);
			int port = appProfile.getPort();
			if (port > 0) {
				profile.setPort(port);
			} else {
				profile.setPort(8080);
			}
			poolName = HedwigUtil.isBlankString(poolName) ? profile.getServiceAppName() : poolName;
			String strService = profile.toString();
			if (register == null) {
				register = RegisterFactory.getRegister(InternalConstants.SERVICE_REGISTER_ZK);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Starting regist service " + strService);
			}
			profile.setServiceEnable(defaultStatus);
			register.regist(profile);
			if (logger.isDebugEnabled()) {
				logger.debug("Ending regist service " + strService);
			}
			logger.info(
					"HedwigExporter publish service success,serviceName:" + profile.getServiceName() + ",url:" + profile.getServiceUrl());
		} catch (Throwable e) {
			logger.error("HedwigExporter can't regist service," + e.getMessage());
		}
	}

	private void checkAppProfile(AppProfile appProfile) {
		if (HedwigUtil.isBlankString(appProfile.getDomainName())) {
			throw new RuntimeException("Domain name must not be blank!!!");
		}

		if (appProfile.getDomainName().contains("${")) {
			throw new RuntimeException(new StringBuilder().append("DomainName[").append(appProfile.getDomainName())
					.append("] contains unresolved placeholder, please fix it first.").toString());
		}

		if (HedwigUtil.isBlankString(appProfile.getServiceAppName())) {
			throw new RuntimeException("ServiceAppName must not blank!!!");
		}

		if (appProfile.getServiceAppName().contains("${")) {
			throw new RuntimeException(new StringBuilder().append("ServiceAppName[").append(appProfile.getServiceAppName())
					.append("] contains unresolved placeholder, please fix it first.").toString());
		}
	}

	private ServiceProfile createServiceProfile() throws InvalidParamException {
		if (appProfile == null) {
			throw new InvalidParamException("appContexts must not blank!!!");
		}
		ServiceProfile p = new ServiceProfile();
		p.setServiceName(serviceName);
		if (HedwigUtil.isBlankString(serviceVersion)) {
			throw new InvalidParamException("serviceVersion must not blank!!!");
		}
		p.setServiceVersion(serviceVersion);

		if (HedwigUtil.isBlankString(p.getHostIp())) {
			throw new InvalidParamException("Unavailable ip address has been retrieved.");
		}
		return p;
	}

	public void changeServEnable(boolean enable) {
		boolean cv = profile.getCurStatus().equals(ServiceStatus.ENABLE);
		if (enable != cv) {
			profile.increaseVersion();
			profile.setServiceEnable(enable);
			register.updateProfile(profile);
		}
	}

	public void changeServWeight(int newWeight) {
		int weight = profile.getWeighted();
		if (newWeight > 0) {
			weight = newWeight > InternalConstants.WEIGHT_LIMIT ? InternalConstants.WEIGHT_LIMIT : newWeight;
		}
		if (weight != profile.getWeighted()) {
			profile.increaseVersion();
			profile.setWeighted(weight);
			register.updateProfile(profile);
		}
	}

	public void setServiceName(String serviceName) {
		this.serviceName = HedwigUtil.filterString(serviceName);
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public void setProfile(ServiceProfile profile) {
		this.profile = profile;
	}

	public AppProfile getAppProfile() {
		return appProfile;
	}

	public void setAppProfile(AppProfile appProfile) {
		this.appProfile = appProfile;
	}

	public void setTpsThreshold(int tpsThreshold) {
		this.tpsThreshold = tpsThreshold;
	}

	public void setDefaultStatus(boolean defaultStatus) {
		this.defaultStatus = defaultStatus;
	}

	@Override
	public void setBeanName(String name) {
		if (name.startsWith("/")) {
			this.serviceName = name.replaceFirst("/", "");
		} else {
			this.serviceName = name;
		}
	}

}
