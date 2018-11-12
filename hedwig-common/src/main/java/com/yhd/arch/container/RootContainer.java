/**
 *
 */
package com.yhd.arch.container;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.photon.common.ProfileUtil;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;
import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * Singleton container, use to store global information of application.
 *
 * @author root
 */
public class RootContainer {

	private static Logger logger = LoggerFactory.getLogger(RootContainer.class);
	private static RootContainer container = new RootContainer();
	private String appName;
	private String contextPath;
	private String hostIp;
	private Map<String, ServiceProfile> serviceProfileMap = new HashMap<String, ServiceProfile>();
	private Map<String, ClientProfile> clientProfileMap = new HashMap<String, ClientProfile>();
	private Map<String, String> servicePoolNameMap = new HashMap<String, String>();

	public static RootContainer getInstance() {
		return container;
	}

	private RootContainer() {
		super();
		appName = YccGlobalPropertyConfigurer.getMainPoolId();
		if (!StringUtils.isPoolid(appName)) {
			logger.error("FATAL_ERROR:Poolid should match pattern xxxx/xxxx," + appName + " is invalidate!!!!!!!!!");
		}
		hostIp = SystemUtil.getLocalhostIp();
	}

	public String getAppName() {
		return appName;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		if (HedwigUtil.isBlankString(contextPath)) {
			this.contextPath = contextPath;
		}
	}

	public String getHostIp() {
		return hostIp;
	}

	public void putServiceProfile(String sName, ServiceProfile profile) {
		String key = ProfileUtil.buildProfileKey(sName, profile.getProfileUUId());
		if (!serviceProfileMap.containsKey(key)) {
			serviceProfileMap.put(key, profile);
		} else {
			throw new RuntimeException(key + " is already existed!!");
		}
	}

	public ServiceProfile getServiceProfile(String sName, String profileUUId) {
		String key = ProfileUtil.buildProfileKey(sName, profileUUId);
		return serviceProfileMap.get(key);
	}

	public ClientProfile getClientProfile(String sName, String profileUUId) {
		String key = ProfileUtil.buildProfileKey(sName, profileUUId);
		return clientProfileMap.get(key);
	}

	public void putClientProfile(String sName, ClientProfile profile) {
		String key = ProfileUtil.buildProfileKey(sName, profile.getProfileUUId());
		if (!clientProfileMap.containsKey(key)) {
			clientProfileMap.put(key, profile);
		} else {
			throw new RuntimeException(key + "is already existed!!");
		}
	}

	public void putServicePoolName(String path, String poolName) {
		if (path != null && poolName != null) {
			this.servicePoolNameMap.put(path, poolName);
		}
	}

	public String getServicePoolName(String path) {
		if (path != null) {
			return this.servicePoolNameMap.get(path);
		} else {
			return null;
		}
	}

	public void clean(){
		serviceProfileMap = new HashMap<String, ServiceProfile>();
		clientProfileMap = new HashMap<String, ClientProfile>();
		servicePoolNameMap = new HashMap<String, String>();
	}

}
