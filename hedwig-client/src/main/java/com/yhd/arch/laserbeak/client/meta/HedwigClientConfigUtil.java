package com.yhd.arch.laserbeak.client.meta;

import com.alibaba.fastjson.JSON;
import com.yhd.arch.photon.invoker.DefaultRequest;
import com.yhd.arch.zone.ZoneContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.util.*;
import com.yihaodian.architecture.zkclient.ZkClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigClientConfigUtil {
    private static Logger logger = LoggerFactory.getLogger(HedwigClientConfigUtil.class);

    public static String buildClientConfigPath(String providerPoolId, String clientPoolId) {
        StringBuilder sb = new StringBuilder();
        String providerPoolName = providerPoolId.replaceAll("/", "#");
        if(StringUtils.isNotBlank(clientPoolId)) {
            String clientPoolName = clientPoolId.replaceAll("/", "#");
            sb.append(InternalConstants.BASE_ROOT_FLAGS).append("/").append(providerPoolName)
                    .append(HedwigClientConfigConstants.FLAG_HEDWIG_CLIENT_CONFIG_PATH)
                    .append("/").append(clientPoolName);
        }else{
            sb.append(InternalConstants.BASE_ROOT_FLAGS).append("/").append(providerPoolName);
        }
        return sb.toString();
    }

    public static String buildClientConfigContainerKey(String clientPoolId, String providerPoolId) {
//        String localZoneName = getLocalZoneName();
        return buildClientConfigContainerKey(null, providerPoolId, null, clientPoolId);
    }

    private static String buildClientConfigContainerKey(String clientZone, String clientPoolId, String providerZone, String providerPoolId) {
        if(StringUtils.isNotBlank(clientPoolId)&&StringUtils.isNotBlank(providerPoolId)) {
            StringBuilder sb = new StringBuilder();
            String clientPoolName = clientPoolId.replaceAll("/", "#");
            String servicePoolName = providerPoolId.replaceAll("/", "#");
            sb.append(servicePoolName).append("@").append(clientPoolName);
            return sb.toString();
        }else{
            return null;
        }
    }

    public static ZkClient getZkClient() {
        ZkClient zkclient = null;
        try {
            zkclient = ZkUtil.getZkClientInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zkclient;
    }

    public static String getLocalZoneName() {
        return ZoneContainer.getInstance().getLocalZoneName();
    }

    public static ClientProfile updateClientProfile(String serviceName, ClientProfile clientProfile) {
        String clientPoolId = clientProfile.getClientPoolId();
        String providerPoolId = clientProfile.getProviderPoolId();
        String key = HedwigClientConfigUtil.buildClientConfigContainerKey(clientPoolId, providerPoolId);
        HedwigClientConfigVo configVo = HedwigClientConfigContainer.getClientConfigVo(key);
        if (configVo != null) {
            HedwigClientConfigDataVo configDataVo = configVo.getConfigDataVoByLevel(serviceName, HedwigClientConfigConstants.DEFAULT_ALL_METHOD_NAME);
            if (configDataVo != null && configDataVo.getDataStatus() == HedwigClientConfigConstants.CONFIG_DATA_STATUS_AUDITED) {
                clientProfile.setTimeout(configDataVo.getTimeout());
                clientProfile.setReadTimeout(configDataVo.getReadTimeout());
                if (configDataVo.getStrGroupName() != null) {
                    clientProfile.setStrGroupName(configDataVo.getStrGroupName());
                } else {
                    clientProfile.setStrGroupName(InternalConstants.HEDWIG_PAHT_REFUGEE);
                }
                clientProfile.setKickout(configDataVo.isKickoutEnabled());
                String domain = clientProfile.getDomainName();
                domain = (HedwigUtil.isBlankString(domain)||InternalConstants.UNKONW_DOMAIN.equals(domain))?configDataVo.getDomainName():domain;
                clientProfile.setDomainName(domain);
                clientProfile.setServiceAppName(configDataVo.getServiceAppName());
                String version = clientProfile.getServiceVersion();
                version = HedwigUtil.isBlankString(version)||"defaultLaserVersion".equals(version)?configDataVo.getServiceVersion():version;
                clientProfile.setServiceVersion(version);

                clientProfile.setBalanceAlgo(configDataVo.getBalanceAlgo());
                clientProfile.setUseBroadcast(configDataVo.isUseBroadCast());
                clientProfile.setClientThrottle(configDataVo.isClientThrottle());

                clientProfile.setUser(configDataVo.getUserName());
                clientProfile.setPassword(configDataVo.getPassword());
            } else {
                logger.error("###---configDataVo=" + HedwigJsonUtil.toJSONString(configDataVo));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("###----configDataVo=" + HedwigJsonUtil.toJSONString(configDataVo));
            }
        }else{
            logger.error("###--hedwigClientConfigVo is null!! key=" + key);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("###----clientProfile=" + JSON.toJSONString(clientProfile));
        }
        return clientProfile;
    }
    //实时动态更新当前请求信息
    public static DefaultRequest<InvocationContext> updateDefaultRequest(DefaultRequest<InvocationContext> inputRequest,ClientProfile profile) {
        DefaultRequest<InvocationContext> resultVo = inputRequest;
        String clientPoolId=profile.getClientPoolId();
        String providerPoolId=profile.getProviderPoolId();
        if(StringUtils.isBlank(clientPoolId) ||StringUtils.isBlank(providerPoolId)) {
            setDefaultClientProfileConfig(inputRequest,profile);
            return resultVo;
        }
        String key = HedwigClientConfigUtil.buildClientConfigContainerKey(clientPoolId, providerPoolId);
        HedwigClientConfigVo configVo = HedwigClientConfigContainer.getClientConfigVo(key);
        if (configVo != null) {
            String serviceName = null;
            if (inputRequest.getServiceInfo() != null && inputRequest.getServiceInfo().getMeta() != null) {
                serviceName = inputRequest.getServiceInfo().getMeta().getServiceName();
            }
            String methodName = inputRequest.getMangleName();
            HedwigClientConfigDataVo configDataVo = configVo.getConfigDataVoByLevel(serviceName, methodName);
            if (configDataVo != null && configDataVo.getDataStatus() == HedwigClientConfigConstants.CONFIG_DATA_STATUS_AUDITED) {
                //线程级别超时时间优先级高于方法级别配置
                Long requestTimeout = HedwigConfigUtil.getRequestTimeout();
                if (requestTimeout != null && requestTimeout > 0L) {
                    resultVo.setTimeout(requestTimeout);
                } else {
                    resultVo.setTimeout(configDataVo.getTimeout());
                }
                //线程级别读取超时时间优先级高于方法级别配置
                Long requestReadTimeout = HedwigConfigUtil.getRequestReadTimeout();
                if (requestReadTimeout != null && requestReadTimeout > 0L) {
                    resultVo.setReadTimeout(requestReadTimeout);
                } else {
                    resultVo.setReadTimeout(configDataVo.getReadTimeout());
                }
                //设置当前是否重试
                resultVo.setRetryAble(configDataVo.isAutoRedo());
                //设置调用开关是否开启
                resultVo.setInvokeEnabled(configDataVo.isInvokeEnabled());
                //是否开启超时踢出功能，默认开启
                resultVo.setKickoutEnabled(configDataVo.isKickoutEnabled());
                //设置权限认证用户名和密码
                if (StringUtils.isNotBlank(configDataVo.getUserName()) && StringUtils.isNotBlank(configDataVo.getPassword())) {
                    resultVo.setSignCode(configDataVo.getSignCode());
                } else {
                    resultVo.setSignCode(null);
                }
                //设置分组信息
                String requestGroupName=HedwigConfigUtil.getRequestGroupName();
                if(StringUtils.isNotBlank(requestGroupName)) {
                    resultVo.setGroupNames(Arrays.asList(requestGroupName));
                }else{
                    resultVo.setGroupNames(configDataVo.getGroupNames());
                }
                //设置限流开关与限流容量
                resultVo.setThrottleEnable(configDataVo.isThrottleEnable());
                resultVo.setThrottleCapacity(configDataVo.getThrottleCapacity());
            } else {
                setDefaultClientProfileConfig(inputRequest,profile);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("###----configDataVo=" + HedwigJsonUtil.toJSONString(configDataVo));
            }
        } else {
            setDefaultClientProfileConfig(inputRequest,profile);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("###----resultVo: invokeEnabled=" + resultVo.isInvokeEnabled()
                    + ",throttleEnable=" + resultVo.isThrottleEnable() + ",throttleCapacity=" + resultVo.getThrottleCapacity() +
                    ",timeout=" + resultVo.getTimeout() + ",readTimeout=" + resultVo.getReadTimeout() + ",retryAble=" + resultVo.isRetryAble());
        }
        return resultVo;
    }

    private static DefaultRequest<InvocationContext> setDefaultClientProfileConfig(DefaultRequest<InvocationContext> inputRequest,ClientProfile profile){
        DefaultRequest<InvocationContext> resultVo = inputRequest;
        //线程级别超时时间优先级高于方法级别配置
        Long requestTimeout = HedwigConfigUtil.getRequestTimeout();
        if (requestTimeout != null && requestTimeout > 0L) {
            resultVo.setTimeout(requestTimeout);
        } else {
            resultVo.setTimeout(profile.getTimeout());
        }
        //线程级别读取超时时间优先级高于方法级别配置
        Long requestReadTimeout = HedwigConfigUtil.getRequestReadTimeout();
        if (requestReadTimeout != null && requestReadTimeout > 0L) {
            resultVo.setReadTimeout(requestReadTimeout);
        } else {
            resultVo.setReadTimeout(profile.getReadTimeout());
        }
        //设置当前是否重试
        resultVo.setRetryAble(profile.isRedoAble());
        //设置调用开关是否开启
        resultVo.setInvokeEnabled(true);
        //是否开启超时踢出功能，默认开启
        resultVo.setKickoutEnabled(profile.isKickout());
        //设置分组信息
        String requestGroupName=HedwigConfigUtil.getRequestGroupName();
        if(StringUtils.isNotBlank(requestGroupName)) {
            resultVo.setGroupNames(Arrays.asList(requestGroupName));
        }else {
            if (profile.getGroupNames() != null && profile.getGroupNames().size() > 0) {
                List<String> groupNames = new ArrayList<String>(profile.getGroupNames());
                resultVo.setGroupNames(groupNames);
            } else {
                List<String> groupNames = new ArrayList<String>(1);
                groupNames.add(InternalConstants.HEDWIG_PAHT_REFUGEE);
                resultVo.setGroupNames(groupNames);
            }
        }
        //设置权限认证用户名和密码
        if (StringUtils.isNotBlank(profile.getUser()) && StringUtils.isNotBlank(profile.getPassword())) {
            resultVo.setSignCode(HedwigUtil.genAuthorization(profile.getUser(), profile.getPassword()));
        }
        return resultVo;
    }
}
