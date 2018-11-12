/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.dto;

import java.io.Serializable;

import com.yhd.arch.photon.constants.ProtocolType;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.hedwig.common.uuid.MD5;

/**
 * @author root
 * 
 */
public class BaseProfile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6856572567927129370L;
	protected String rootPath = InternalConstants.BASE_ROOT;
	protected String domainName = InternalConstants.UNKONW_DOMAIN;
	protected String parentPath;
	protected String serviceAppName = "defaultLaserAppName";
	protected String serviceName = "defaultLaserServiceName";
	protected String serviceVersion = "defaultLaserVersion";
	protected ProtocolType transProtocol = ProtocolType.HTTP;
	protected String profileUUId = "";

	public BaseProfile() {
		super();
	}

	public String getRootPath() {
		return rootPath;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = HedwigUtil.filterString(domainName);
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getServiceAppName() {
		return serviceAppName;
	}

	public void setServiceAppName(String serviceAppName) {
		this.serviceAppName = HedwigUtil.filterString(serviceAppName);
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = HedwigUtil.filterString(serviceName);
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = HedwigUtil.filterString(serviceVersion);
	}

	public ProtocolType getTransProtocol() {
		return transProtocol == null ? ProtocolType.HTTP : transProtocol;
	}

	public void setTransProtocol(ProtocolType transProtocol) {
		this.transProtocol = transProtocol;
	}

	public String getProfileUUId() {
		if (HedwigUtil.isBlankString(profileUUId)) {
			StringBuilder sb = new StringBuilder();
			if (!HedwigUtil.isBlankString(domainName)) {
				sb.append(domainName);
			}
			if (!HedwigUtil.isBlankString(serviceAppName)) {
				sb.append(serviceAppName);
			}
			if (!HedwigUtil.isBlankString(serviceVersion)) {
				sb.append(serviceVersion);
			}
			profileUUId = MD5.getInstance().getMD5String(sb.toString());
		}

		return profileUUId;
	}

	public String getParentPath() {
		if (HedwigUtil.isBlankString(parentPath)) {
			try {
				parentPath = ZkUtil.createParentPath(this);
			} catch (InvalidParamException e) {

			}
		}
		return parentPath;
	}

	@Override
	public String toString() {
		return "BaseProfile [rootPath=" + rootPath + ", domainName=" + domainName + ", parentPath=" + parentPath + ", serviceAppName="
				+ serviceAppName + ", serviceName=" + serviceName + ", serviceVersion=" + serviceVersion + ", transProtocol="
				+ transProtocol + "]";
	}

}
