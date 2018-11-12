package com.yhd.arch.laserbeak.client.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户自定义客户端pool名称和服务端pool名称（特殊场景下使用）
 *
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigClientConfigPlugin {
    private static Logger logger = LoggerFactory.getLogger(HedwigClientConfigPlugin.class);
    private static String clientPoolId;// 客户端配置中心中的poolId
    private static String providerPoolId;// 服务提供方配置中心中的poolId（新增）

    public HedwigClientConfigPlugin() {
        if (logger.isDebugEnabled()) {
            logger.debug("#####  HedwigClientConfigPlugin Init SUCCESS!!!  #######");
        }
    }

    public static String getClientPoolId() {
        return clientPoolId;
    }

    public void setClientPoolId(String clientPoolId) {
        this.clientPoolId = clientPoolId;
    }

    public static String getProviderPoolId() {
        return providerPoolId;
    }

    public void setProviderPoolId(String providerPoolId) {
        this.providerPoolId = providerPoolId;
    }
}
