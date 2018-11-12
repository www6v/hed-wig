/**
 * 
 */
package com.yhd.arch.laserbeak.provider;

import com.yhd.arch.photon.constants.Constants;
import com.yhd.arch.photon.constants.PhotonPropKeys;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;

/**
 * @author root
 *
 */
public class AppMeta {
	private String domainName = InternalConstants.UNKONW_DOMAIN;
	private String serviceAppName = "defaultAppName";
	private int port = -1;
	private String user;
	private String password;
	private boolean isThottle = false;
	private int tps = 3000;

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getServiceAppName() {
		return serviceAppName;
	}

	public void setServiceAppName(String serviceAppName) {
		this.serviceAppName = serviceAppName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isThottle() {
		return isThottle;
	}

	public void setThottle(boolean isThottle) {
		this.isThottle = isThottle;
		if (isThottle) {
			System.setProperty(PhotonPropKeys.KEY_THROTTLE_ENABLE, Constants.ENABLE);
		}

	}

	public int getTps() {
		return tps;
	}

	public void setTps(int tps) {
		if (tps > 0) {
			if (!this.isThottle) {
				setThottle(true);
			}
			System.setProperty(PhotonPropKeys.KEY_THROTTLE_TPS, tps + "");
			this.tps = tps;
		}

	}

}
