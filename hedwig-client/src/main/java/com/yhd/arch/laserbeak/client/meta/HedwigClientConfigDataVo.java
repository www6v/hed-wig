package com.yhd.arch.laserbeak.client.meta;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigClientConfigDataVo implements Serializable {
    private static final long serialVersionUID = -3449736799079134998L;
    private String clientZoneId;//客户端所在Zone名称Id
    private String clientPoolId;//客户端   poolId
    private String providerZoneId;//服务提供方所在Zone名称Id
    private String providerPoolId;//服务提供方pool名称Id

    private String serviceName;//服务接口名称
    private String methodName;//服务方法名称

    private long timeout;//客户端最大所允许的超时时间
    private long readTimeout;//客户端读取超时时间
    private List<String> groupNames;//客户端调用的对应服务分组名称
    private String strGroupName;//客户端设置的分组字符串
    private boolean autoRedo = false;//客户端调用这个方法是否允许重试，默认不重试
    private boolean invokeEnabled = true;//客户端是否允许调用方法开关
    private boolean kickoutEnabled=true;//是否开启超时踢出功能，默认开启（新增）
    private String logPackageName;//客户端hedwig日志打印包路径
    private String logLevel;//客户端hedwig日志打印级别
    private String balanceAlgo = InternalConstants.BALANCER_NAME_WEIGHTED_ROUNDROBIN;//客户端负载均衡算法名称（重启生效）
    private boolean clientThrottle = false;//客户端是否需要进行限流(废弃）
    private boolean throttleEnable=true;//客户端MethodActor限流是否开启
    private int throttleCapacity=-1;//客户端MethodActor限流容量
    protected String userName;//客户端hessian权限认证用户名
    protected String password;//客户端hessian权限认证用户密码
    private String signCode;//根据用户名和密码生成的权限认证码
    private String clientVersion;//客户端版本号
    private String clientAppName;//客户端应用名称

    private boolean useBroadCast = false;//客户端是否使用广播模式(废弃）

    private String domainName;//服务提供方所属domain名称
    private String serviceVersion;//服务提供方服务版本号
    private String serviceAppName;//服务提供方应用名称

    private int dataStatus=0;//数据状态 0:未审核 1:审核中 2:审核通过
    private int applyType = 0;//申请操作类型 -1:删除 0:新增 1:修改

    public String getClientZoneId() {
        return clientZoneId;
    }

    public void setClientZoneId(String clientZoneId) {
        this.clientZoneId = clientZoneId;
    }

    public String getClientPoolId() {
        return clientPoolId;
    }

    public void setClientPoolId(String clientPoolId) {
        this.clientPoolId = clientPoolId;
    }

    public String getProviderZoneId() {
        return providerZoneId;
    }

    public void setProviderZoneId(String providerZoneId) {
        this.providerZoneId = providerZoneId;
    }

    public String getProviderPoolId() {
        return providerPoolId;
    }

    public void setProviderPoolId(String providerPoolId) {
        this.providerPoolId = providerPoolId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isAutoRedo() {
        return autoRedo;
    }

    public void setAutoRedo(boolean autoRedo) {
        this.autoRedo = autoRedo;
    }

    public boolean isInvokeEnabled() {
        return invokeEnabled;
    }

    public void setInvokeEnabled(boolean invokeEnabled) {
        this.invokeEnabled = invokeEnabled;
    }

    public boolean isKickoutEnabled() {
        return kickoutEnabled;
    }

    public void setKickoutEnabled(boolean kickoutEnabled) {
        this.kickoutEnabled = kickoutEnabled;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogPackageName() {
        return logPackageName;
    }

    public void setLogPackageName(String logPackageName) {
        this.logPackageName = logPackageName;
    }

    public String getBalanceAlgo() {
        return balanceAlgo;
    }

    public void setBalanceAlgo(String balanceAlgo) {
        this.balanceAlgo = balanceAlgo;
    }

    public boolean isClientThrottle() {
        return clientThrottle;
    }

    public void setClientThrottle(boolean clientThrottle) {
        this.clientThrottle = clientThrottle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getClientAppName() {
        return clientAppName;
    }

    public void setClientAppName(String clientAppName) {
        this.clientAppName = clientAppName;
    }

    public boolean isUseBroadCast() {
        return useBroadCast;
    }

    public void setUseBroadCast(boolean useBroadCast) {
        this.useBroadCast = useBroadCast;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName =  HedwigUtil.filterString(domainName);
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = HedwigUtil.filterString(serviceVersion);
    }

    public String getServiceAppName() {
        return serviceAppName;
    }

    public void setServiceAppName(String serviceAppName) {
        this.serviceAppName = HedwigUtil.filterString(serviceAppName);
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setStrGroupName(String groupName) {
        this.strGroupName=groupName;
        if (!HedwigUtil.isBlankString(groupName)) {
            String[] sArr = groupName.split(InternalConstants.STRING_SEPARATOR);
            Set<String> nameSet = new HashSet<String>();
            for (String name : sArr) {
                nameSet.add(name);
            }
            List<String> nameList=new ArrayList<String>();
            nameList.addAll(nameSet);
            this.groupNames = nameList;
        }
    }

    public String getStrGroupName() {
        return this.strGroupName;
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public int getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(int dataStatus) {
        this.dataStatus = dataStatus;
    }

    public boolean isThrottleEnable() {
        return throttleEnable;
    }

    public void setThrottleEnable(boolean throttleEnable) {
        this.throttleEnable = throttleEnable;
    }

    public int getThrottleCapacity() {
        return throttleCapacity;
    }

    public void setThrottleCapacity(int throttleCapacity) {
        this.throttleCapacity = throttleCapacity;
    }

    public int getApplyType() {
        return applyType;
    }

    public void setApplyType(int applyType) {
        this.applyType = applyType;
    }
}