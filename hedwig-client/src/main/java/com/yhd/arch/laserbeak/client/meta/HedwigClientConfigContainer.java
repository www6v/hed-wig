package com.yhd.arch.laserbeak.client.meta;

import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigClientConfigContainer {
    private static Logger logger = LoggerFactory.getLogger(HedwigClientConfigContainer.class);
    //key=providerPoolId_clientPoolId value=配置信息
    private static Map<String, HedwigClientConfigVo> clientConfigVoMap = new HashMap<String, HedwigClientConfigVo>();

    private static ZkClient zkclient;

    static {
        try {
            zkclient = ZkUtil.getZkClientInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void putClientConfigVo(String key, HedwigClientConfigVo configVo) {
        clientConfigVoMap.put(key, configVo);
    }

    public static void removeClientConfigVo(String key) {
        clientConfigVoMap.remove(key);
    }

    public static HedwigClientConfigVo getClientConfigVo(String key) {
        return clientConfigVoMap.get(key);
    }

    public static void saveClientConfigVoToZK(String providerPoolId, String clientPoolId, HedwigClientConfigVo configVo) {
        String configPath = HedwigClientConfigUtil.buildClientConfigPath(providerPoolId, clientPoolId);
        if (!zkclient.exists(configPath)) {
            zkclient.createPersistent(configPath, true);
        }
        zkclient.writeData(configPath, configVo);
    }

    public static HedwigClientConfigVo getClientConfigVoFromZK(String providerPoolId, String clientPoolId) {
        HedwigClientConfigVo resultVo = null;
        String configPath = HedwigClientConfigUtil.buildClientConfigPath(providerPoolId, clientPoolId);
        if (zkclient.exists(configPath)) {
            resultVo = zkclient.readData(configPath);
        }
        return resultVo;
    }

    public static boolean deleteClientConfigVoFromZK(String providerPoolId, String clientPoolId) {
        boolean resultVo = false;
        String configPath = HedwigClientConfigUtil.buildClientConfigPath(providerPoolId, clientPoolId);
        if (zkclient.exists(configPath)) {
            resultVo = zkclient.deleteRecursive(configPath);
        }
        return resultVo;
    }
}
