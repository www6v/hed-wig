/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yhd.arch.photon.constants.ProtocolType;
import com.yhd.arch.photon.constants.SupportedCodec;
import com.yhd.arch.zone.RoutePriority;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.constants.ServiceStatus;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.RelivePolicy;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;

/**
 * @author root
 * 
 */
public class ServiceProfile extends BaseProfile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6012531717460254654L;
	private String servicePath;

	/**
	 * 服务URL
	 */
	private String serviceUrl;

	/**
	 * 协议前缀
	 */
	private String protocolPrefix = InternalConstants.PROTOCOL_PROFIX_HTTP;
	/**
	 * 转发用规则
	 */
	private String urlPattern = InternalConstants.HEDWIG_URL_PATTERN;
	/**
	 * 机器IP
	 */
	private String hostIp;
	/**
	 * 进程ID
	 */
	private String jvmPid;
	/**
	 * 服务端口
	 */
	private int port = -1;
	/**
	 * 服务元数据版本
	 */
	private int revision = 0;
	/**
	 * 默认权重
	 */
	private int weighted = 1;
	/**
	 * 负载
	 */
	private double loadRate = 0.0d;
	/**
	 * 负载阀值
	 */
	private double loadThreshold = 0.9d;
	/**
	 * 当前权重
	 */
	private AtomicInteger curWeight = new AtomicInteger(weighted);
	/**
	 * 服务状态
	 */
	private AtomicInteger status = new AtomicInteger(1);
	private volatile ServiceStatus curStatus = ServiceStatus.ENABLE;
	private AtomicBoolean available = new AtomicBoolean(true);
	/**
	 * 复活策略
	 */
	private transient RelivePolicy relivePolicy;;
	/**
	 * 注册时间
	 */
	private Date registTime;
	/**
	 * 是否拼装appname到serviceurl中
	 */
	private boolean assembleAppName = false;

	private String searalizeTrigger = "";

	private String codecName = SupportedCodec.HEDWIG;

	private Lock lock = new ReentrantLock();

	/**
	 * Service publish zone.
	 */
	private String pubZone;

	/**
	 * Service regist zone.
	 */
	private String regZone;

	/**
	 * Service publish idc.
	 */
	private String pubIdc;

	/**
	 * Service regist idc.
	 */
	private String regIdc;

	private String providerLevel;

	/**
	 * Service publish poolName
	 */
	private String pubPoolName;

	private Map<String, RoutePriority> methodRP;

	private List<String> mehodNames;

	public ServiceProfile() {
		super();
		ProperitesContainer container = ProperitesContainer.provider();
		hostIp = container.getProperty(PropKeyConstants.HOST_IP);
		jvmPid = container.getProperty(PropKeyConstants.JVM_PID);
		rootPath = container.getProperty(PropKeyConstants.ZK_ROOT_PATH, rootPath);
		weighted = HedwigUtil.parseString2Int(container.getProperty(PropKeyConstants.HOST_WEIGHTED), 1);
		weighted = weighted <= 0 ? 1 : weighted;
		weighted = weighted > 20 ? 20 : weighted;
		methodRP = new HashMap<String, RoutePriority>();
	}

	public void setTransProtocol(ProtocolType transProtocol) {
		super.setTransProtocol(transProtocol);
		this.protocolPrefix = transProtocol.getPrefix();
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getServiceUrl() {
		if (HedwigUtil.isBlankString(serviceUrl)) {
			serviceUrl = HedwigUtil.generateServiceUrl(this);
		}
		return serviceUrl;
	}

	public String getProtocolPrefix() {
		return protocolPrefix;
	}

	public void setProtocolPrefix(String protocolPrefix) {
		this.protocolPrefix = protocolPrefix;
	}

	public String getJvmPid() {
		return jvmPid;
	}

	public void setJvmPid(String jvmPid) {
		this.jvmPid = jvmPid;
	}

	public AtomicInteger getCurWeight() {
		return curWeight;
	}

	public void setCurWeight(AtomicInteger curWeight) {
		this.curWeight = curWeight;
	}

	public void decreaseCurWeight() {
		curWeight.decrementAndGet();
	}

	public void resetCurWeight() {
		curWeight = new AtomicInteger(weighted);
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public int getWeighted() {
		return weighted;
	}

	public void setWeighted(int weighted) {
		this.weighted = weighted;
		this.curWeight = new AtomicInteger(weighted);
	}

	public double getLoadRate() {
		return loadRate;
	}

	public void setLoadRate(double loadRate) {
		this.loadRate = loadRate;
	}

	public double getLoadThreshold() {
		return loadThreshold;
	}

	public void setLoadThreshold(double loadThreshold) {
		this.loadThreshold = loadThreshold;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = ProperitesContainer.provider().getIntProperty(PropKeyConstants.HOST_PORT, port);
	}

	public int getCurWeighted() {
		return curWeight.get();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			String pPath = ZkUtil.createParentPath(this);
			sb.append("parentPath:").append(pPath);
			sb.append("; serviceUrl:").append(getServiceUrl());
		} catch (InvalidParamException e) {
		}
		return sb.toString();
	}

	public String getServicePath() {
		if (HedwigUtil.isBlankString(servicePath)) {
			try {
				servicePath = ZkUtil.createChildPath(this);
			} catch (Exception e) {

			}
		}
		return servicePath;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	public boolean isAvailable() {
		boolean value = true;
		curStatus = curStatus == null ? ServiceStatus.ENABLE : curStatus;
		if (curStatus.equals(ServiceStatus.DISENABLE)) {
			value = false;
		} else if (curStatus.equals(ServiceStatus.TEMPORARY_DISENABLE) && relivePolicy != null) {
			lock.lock();
			try {
				if (curStatus.equals(ServiceStatus.TEMPORARY_DISENABLE)) {
					value = relivePolicy.tryRelive();
				}
			} finally {
				lock.unlock();
			}
		}
		return value;
	}

	public ServiceStatus getCurStatus() {
		return curStatus;
	}

	public void setCurStatus(ServiceStatus curStatus) {
		if (this.relivePolicy != null && this.curStatus.equals(ServiceStatus.TEMPORARY_DISENABLE) && curStatus.equals(ServiceStatus.ENABLE)) {
			this.relivePolicy.reset();
		}
		this.curStatus = curStatus;
	}

	public void setRelivePolicy(RelivePolicy relivePolicy) {
		this.relivePolicy = relivePolicy;
	}

	public AtomicBoolean getAvailable() {
		return available;
	}

	public void setAvailable(AtomicBoolean available) {
		this.available = available;
	}

	public Date getRegistTime() {
		return registTime;
	}

	public void setRegistTime(Date registTime) {
		this.registTime = registTime;
	}

	public boolean isAssembleAppName() {
		return assembleAppName;
	}

	public void setAssembleAppName(boolean assembleAppName) {
		this.assembleAppName = assembleAppName;
	}

	public AtomicInteger getStatus() {
		return status;
	}

	public void setStatus(AtomicInteger status) {
		this.status = status;
	}

	public String getCodecName() {
		return codecName == null ? SupportedCodec.HEDWIG : codecName;
	}

	public void setCodecName(String codecName) {
		this.codecName = codecName;
	}

	public void increaseVersion() {
		if (revision < Integer.MAX_VALUE) {
			this.revision++;
		}
	}

	public void update(ServiceProfile newProfile) {
		if (newProfile != null) {
			this.setCurStatus(newProfile.getCurStatus());
			this.setLoadRate(newProfile.getLoadRate());
			this.setLoadThreshold(newProfile.getLoadThreshold());
			this.setRegistTime(newProfile.getRegistTime());
			this.setRevision(newProfile.getRevision());
			this.setWeighted(newProfile.getWeighted());
			this.setPubPoolName(newProfile.getPubPoolName());
		}
	}

	public String getHostString() {
		return new StringBuilder(hostIp).append(":").append(port).toString();
	}

	public void setServiceEnable(boolean enable) {
		if (enable) {
			setCurStatus(ServiceStatus.ENABLE);
		} else {
			setCurStatus(ServiceStatus.DISENABLE);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
		result = prime * result + weighted;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceProfile other = (ServiceProfile) obj;
		if (serviceUrl == null) {
			if (other.serviceUrl != null)
				return false;
		} else if (!serviceUrl.equals(other.serviceUrl))
			return false;
		if (weighted != other.weighted)
			return false;
		if (!curStatus.equals(other.getCurStatus()))
			return false;
		if (loadRate != loadRate)
			return false;
		return true;
	}

	public String getPubZone() {
		return pubZone;
	}

	public void setPubZone(String pubZone) {
		this.pubZone = pubZone;
	}

	public String getPubIdc() {
		return pubIdc;
	}

	public void setPubIdc(String pubIdc) {
		this.pubIdc = pubIdc;
	}

	public String  getProviderLevel(){
		return providerLevel;
	}

	public void setProviderLevel(String providerLevel){
		this.providerLevel=providerLevel;
	}

	public String getRegZone() {
		return regZone;
	}

	public void setRegZone(String regZone) {
		this.regZone = regZone;
	}

	public String getRegIdc() {
		return regIdc;
	}

	public void setRegIdc(String regIdc) {
		this.regIdc = regIdc;
	}

	public String getPubPoolName() {
		return pubPoolName;
	}

	public void setPubPoolName(String pubPoolName) {
		this.pubPoolName = pubPoolName;
	}

	public Map<String, RoutePriority> getMethodRP() {
		return methodRP;
	}

	public void setMethodRP(Map<String, RoutePriority> methodRP) {
		this.methodRP = methodRP;
	}

	public List<String> getMehodNames() {
		return mehodNames;
	}

	public void setMehodNames(List<String> mehodNames) {
		this.mehodNames = mehodNames;
	}

	public String getSearalizeTrigger() {
		return this.searalizeTrigger;
	}

	public void setSearalizeTrigger(String searalizeTrigger) {
		this.searalizeTrigger = searalizeTrigger;
	}

	@Override
	public ServiceProfile clone() throws CloneNotSupportedException {
		ServiceProfile sp = new ServiceProfile();
		sp.setAssembleAppName(assembleAppName);
		sp.setAvailable(available);
		sp.setCodecName(codecName);
		sp.setCurStatus(curStatus);
		sp.setDomainName(domainName);
		sp.setHostIp(hostIp);
		sp.setJvmPid(jvmPid);
		sp.setLoadRate(loadRate);
		sp.setLoadThreshold(loadThreshold);
		sp.setMehodNames(mehodNames);
		sp.setPort(port);
		sp.setProtocolPrefix(protocolPrefix);
		sp.setPubZone(pubZone);
		sp.setRegistTime(registTime);
		sp.setRootPath(rootPath);
		sp.setServiceAppName(serviceAppName);
		sp.setServiceName(serviceName);
		sp.setServiceUrl(serviceUrl);
		sp.setServiceVersion(serviceVersion);
		sp.setTransProtocol(transProtocol);
		sp.setUrlPattern(urlPattern);
		sp.setWeighted(weighted);
		sp.setPubPoolName(pubPoolName);
		sp.setSearalizeTrigger(searalizeTrigger);
		return sp;
	}

}
