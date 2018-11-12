/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.yhd.arch.photon.constants.ProtocolType;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.constants.RequestType;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 * 
 */
public class ClientProfile extends BaseProfile implements Serializable {
	private static final long serialVersionUID = -5339046889514181081L;
	private String balanceAlgo = InternalConstants.BALANCER_NAME_WEIGHTED_ROUNDROBIN;
	private String target = "";
	private long timeout = ProperitesContainer.client().getLongProperty(PropKeyConstants.HEDWIG_READ_TIMEOUT,
			InternalConstants.DEFAULT_READ_TIMEOUT);
	private boolean profileSensitive = ProperitesContainer.client().getBoolean(PropKeyConstants.HEDWIG_PROFILE_SENSITIVE, false);
	private String requestType = RequestType.SyncPool.getName();
	private String clientAppName;
	private String clientVersion;
	private boolean redoAble = false;
	private Set<String> noRetryMethods;
	private Set<String> groupNames;
	protected String user;
	protected String password;
	protected boolean chunkedPost = true;
	protected boolean overloadedEnable = false;
	protected long readTimeout = ProperitesContainer.client().getLongProperty(PropKeyConstants.HEDWIG_READ_TIMEOUT,
			InternalConstants.DEFAULT_READ_TIMEOUT);
	protected boolean clientThrottle = true;
    private boolean kickout = true;//是否开启超时踢出功能 true：开启 false:不开启
	protected boolean useBroadcast = false;
	protected Class serviceInterface;
	protected int senderCount;

	private String clientPoolId;//客户端配置中心中的poolId（新增）（如果没有默认读取配置中心里面的当前poolId）
	private String providerPoolId;//服务提供方配置中心中的poolId（新增）

	public boolean isUseBroadcast() {
		return useBroadcast;
	}

	public void setUseBroadcast(boolean useBroadcast) {
		this.useBroadcast = useBroadcast;
		if (useBroadcast) {
			balanceAlgo = InternalConstants.BALANCER_NAME_ROUNDROBIN;
		}
	}

	public Set<String> getGroupNames() {
		return groupNames;
	}

	public boolean isRedoAble() {
		return redoAble;
	}

	public void setRedoAble(boolean redoAble) {
		this.redoAble = redoAble;
	}

	public void setGroupNames(Set<String> groupNames) {
		this.groupNames = groupNames;
	}

	public void setStrGroupName(String groupName) {
		if (!HedwigUtil.isBlankString(groupName)) {
			String[] sArr = groupName.split(InternalConstants.STRING_SEPARATOR);
			Set<String> nameSet = new HashSet<String>();
			for (String name : sArr) {
				nameSet.add(name);
			}
			this.groupNames = nameSet;
		}
	}

	public ClientProfile() {
		super();
		ProperitesContainer.client();
		this.clientAppName = YccGlobalPropertyConfigurer.getMainPoolId();
		if (HedwigUtil.isBlankString(this.clientAppName)) {
			this.clientAppName = System.getProperty("clientAppName");
		}
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		if (target != null) {
			this.target = target;
			if(target.startsWith(ProtocolType.HTTP.getPrefix())){
				String[] arr = target.split("/", 6);
				if (arr != null && arr.length > 0) {
					this.serviceName = arr[arr.length - 1];
				}	
			}
		}
	}

	public String getBalanceAlgo() {
		return balanceAlgo;
	}

	public void setBalanceAlgo(String balanceAlgo) {
		this.balanceAlgo = balanceAlgo;
	}

	public boolean isProfileSensitive() {
		return profileSensitive;
	}

	public void setProfileSensitive(boolean profileSensitive) {
		this.profileSensitive = profileSensitive;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		if (timeout > 0) {
			timeout = (timeout >= InternalConstants.DEFAULT_REQUEST_TIMEOUT) ? timeout : InternalConstants.DEFAULT_REQUEST_TIMEOUT;
		}
		this.timeout = timeout;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getClientAppName() {
		return clientAppName;
	}

	public void setClientAppName(String clientAppName) {
		if (HedwigUtil.isBlankString(this.clientAppName)) {
			this.clientAppName = clientAppName;
		}
	}

	public Set<String> getNoRetryMethods() {
		return noRetryMethods;
	}

	public void setNoRetryMethods(Set<String> noRetryMethods) {
		this.noRetryMethods = noRetryMethods;
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

	public boolean isChunkedPost() {
		return chunkedPost;
	}

	public void setChunkedPost(boolean chunkedPost) {
		this.chunkedPost = chunkedPost;
	}

	public long getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = (readTimeout > InternalConstants.DEFAULT_READ_TIMEOUT) ? readTimeout : InternalConstants.DEFAULT_READ_TIMEOUT;
	}

	public boolean isOverloadedEnable() {
		return overloadedEnable;
	}

	public void setOverloadedEnable(boolean overloadedEnable) {
		this.overloadedEnable = overloadedEnable;
	}

	public String getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	public boolean isClientThrottle() {
		return clientThrottle;
	}

	public void setClientThrottle(boolean clientThrottle) {
		this.clientThrottle = clientThrottle;
        this.kickout=clientThrottle;
	}

    public boolean isKickout() {
        return kickout;
    }

    public void setKickout(boolean kickout) {
        this.kickout = kickout;
    }

    public Class getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public int getSenderCount() {
		return senderCount;
	}

	public void setSenderCount(int senderCount) {
		this.senderCount = senderCount;
	}

	public String getClientPoolId() {
		return clientPoolId;
	}

	public void setClientPoolId(String clientPoolId) {
		this.clientPoolId = clientPoolId;
	}

	public String getProviderPoolId() {
		return providerPoolId;
	}

	public void setProviderPoolId(String providerPoolId) {
		this.providerPoolId = providerPoolId;
	}

	@Override
	public String toString() {
		return "ClientProfile [balanceAlgo=" + balanceAlgo + ", target=" + target + ", timeout=" + timeout + ", profileSensitive="
				+ profileSensitive + ", requestType=" + requestType + ", clientAppName=" + clientAppName + ", clientVersion="
				+ clientVersion + ", redoAble=" + redoAble + ", noRetryMethods=" + noRetryMethods + ", groupNames=" + groupNames
				+ ", chunkedPost=" + chunkedPost + ", overloadedEnable=" + overloadedEnable + ", readTimeout=" + readTimeout
				+ ", clientThrottle=" + clientThrottle + ", useBroadcast=" + useBroadcast + ", serviceInterface=" + serviceInterface
				+ ", senderCount=" + senderCount + ", rootPath=" + rootPath + ", domainName=" + domainName + ", parentPath=" + parentPath
				+ ", serviceAppName=" + serviceAppName + ", serviceName=" + serviceName + ", serviceVersion=" + serviceVersion
				+ ", transProtocol=" + transProtocol + "]";
	}
}
