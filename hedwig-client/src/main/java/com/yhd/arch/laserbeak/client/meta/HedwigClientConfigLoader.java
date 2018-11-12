package com.yhd.arch.laserbeak.client.meta;

import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigJsonUtil;
import com.yihaodian.architecture.hedwig.common.util.LogManagerUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * hedwig远程客户配置加载器
 *
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigClientConfigLoader {
    private static Logger logger = LoggerFactory.getLogger(HedwigClientConfigLoader.class);
    private String clientPoolId;
    private String providerPoolId;
    private static ReentrantLock lock = new ReentrantLock();
    private static Map<String, HedwigClientConfigLoader> loaderMap = new HashMap<String, HedwigClientConfigLoader>();
    private boolean isLoaded = false;

    private HedwigClientConfigLoader(String clientPoolId, String providerPoolId) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("###clientPoolId=" + clientPoolId + ",providerPoolId=" + providerPoolId);
        }
        this.clientPoolId = clientPoolId;
        this.providerPoolId = providerPoolId;
        if(logger.isInfoEnabled()) {
            logger.info("---### " + this.toString() + "--- init ### ---");
        }
    }


    public static HedwigClientConfigLoader getLoader(String clientPoolId, String providerPoolId) throws Exception {
        HedwigClientConfigLoader resultLoader = null;
        //用户自定义的配置优先级最高
        if(StringUtils.isNotBlank(HedwigClientConfigPlugin.getClientPoolId())){
            clientPoolId = HedwigClientConfigPlugin.getClientPoolId();
        }
        if(StringUtils.isNotBlank(HedwigClientConfigPlugin.getProviderPoolId())){
            providerPoolId=HedwigClientConfigPlugin.getProviderPoolId();
        }
        if(StringUtils.isNotBlank(providerPoolId)){
            try {
                lock.lock();
                //预先检查poolId和修正poolId
                String checkedClientPoolId = checkClientPoolId(clientPoolId);
                String checkedProviderPoolId = checkProviderPoolId(providerPoolId);
                if (StringUtils.isNotBlank(checkedClientPoolId) && StringUtils.isNotBlank(checkedProviderPoolId)) {
                    String key = HedwigClientConfigUtil.buildClientConfigContainerKey(checkedClientPoolId, checkedProviderPoolId);
                    if (loaderMap.get(key) == null) {
                        HedwigClientConfigLoader loader = new HedwigClientConfigLoader(checkedClientPoolId, checkedProviderPoolId);
                        loaderMap.put(key, loader);
                        resultLoader = loader;
                    } else {
                        resultLoader = loaderMap.get(key);
                    }
                }
            } finally {
                lock.unlock();
            }
        }else{
            logger.warn("### providerPoolId is NULL!!providerPoolId="+providerPoolId);
        }
        return resultLoader;
    }

    private static String checkClientPoolId(String clientPoolId) throws Exception {
        String checkedClientPoolId = null;
        //客户端pooId优先读取用户配置的
        if (StringUtils.isBlank(clientPoolId)) {
            String mainPoolId = YccGlobalPropertyConfigurer.getMainPoolId();
            if (StringUtils.isBlank(mainPoolId)) {
                logger.error("###YccGlobalPropertyConfigurer.getMainPoolId() is NULL!!, getMainPoolId=" + clientPoolId);
            } else {
                checkedClientPoolId = mainPoolId;
            }
        } else {
            checkedClientPoolId = clientPoolId;
        }
        if (StringUtils.isBlank(checkedClientPoolId)) {
            throw new InvalidParamException("### clientPoolId is NULL!!");
        }
        return checkedClientPoolId;
    }

    private static String checkProviderPoolId(String providerPoolId) throws Exception {
        String checkedProviderPoolId = null;
        if (StringUtils.isBlank(providerPoolId)) {
            throw new InvalidParamException("### providerPoolId is NULL!!");
        } else {
            checkedProviderPoolId = providerPoolId;
        }
        return checkedProviderPoolId;
    }

    /**
     * 加载远程配置到内存
     */
    public boolean load() throws Exception {
        boolean result = false;
        try {
            lock.lock();
            if (isLoaded == false) {
                String dataKey = HedwigClientConfigUtil.buildClientConfigContainerKey(clientPoolId, providerPoolId);
                //若之前已经加载，就不再加载，若没有加载则执行加载动作
                if (HedwigClientConfigContainer.getClientConfigVo(dataKey) == null) {
                    //优先先读取自己pool 对应服务配置
                    String configPath = HedwigClientConfigUtil.buildClientConfigPath(providerPoolId, clientPoolId);
                    HedwigClientConfigVo configVo = null;
                    ZkClient zkClient = HedwigClientConfigUtil.getZkClient();
                    if (zkClient.exists(configPath)) {
                        configVo = (HedwigClientConfigVo) zkClient.readData(configPath);
                        //监听节点数据变化
                        subscribeClientConfigVoChange(dataKey,providerPoolId, clientPoolId);
                    } else {
                        //如果不存在，则读取这个服务所有client默认配置
                        configPath = HedwigClientConfigUtil.buildClientConfigPath(providerPoolId, HedwigClientConfigConstants.DEFAULT_ALL_CLIENT_POOL);
                        if (zkClient.exists(configPath)) {
                            configVo = (HedwigClientConfigVo) zkClient.readData(configPath);
                            //监听节点数据变化
                            subscribeClientConfigVoChange(dataKey,providerPoolId, HedwigClientConfigConstants.DEFAULT_ALL_CLIENT_POOL);
                        }
                    }
                    if(configVo==null){
                        logger.warn("###Can't found Remote Hedwig Client Config In ZK,Use local!! configPath=" + configPath + ",configVo=null");
                    }else {
                        //注：加载逻辑，在容器里，自定义值若没有，会用全局的值，但key依然用自定义key
                        HedwigClientConfigContainer.putClientConfigVo(dataKey, configVo);
                        result = true;
                        isLoaded = true;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("###configPath=" + configPath + ",configVo=" + HedwigJsonUtil.toJSONString(configVo));
                    }
                }
            }else{
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            lock.unlock();
        }
        return result;
    }

    private void subscribeClientConfigVoChange(final String dataKey,String providerPoolId, String clientPoolId) {
        try {
            String configPath = HedwigClientConfigUtil.buildClientConfigPath(providerPoolId, clientPoolId);
            ZkClient localZk = ZkUtil.getZkClientInstance();
            localZk.subscribeDataChanges(configPath, new IZkDataListener() {
                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {
                    HedwigClientConfigVo configVo = (HedwigClientConfigVo) data;
                    HedwigClientConfigContainer.putClientConfigVo(dataKey, configVo);
                    updateHedwigClientLogLevel(configVo, false);
                    logger.error("###HedwigClientConfigChange:handleDataChange dataPath=" + dataPath + ",data=" + HedwigJsonUtil.toJSONString(data));
                }

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    HedwigClientConfigVo configVo = HedwigClientConfigContainer.getClientConfigVo(dataKey);
                    updateHedwigClientLogLevel(configVo, true);
                    HedwigClientConfigContainer.removeClientConfigVo(dataKey);
                    logger.error("###HedwigClientConfigChange:handleDataDeleted dataPath=" + dataPath);
                }
            });
            if (logger.isDebugEnabled()) {
                logger.debug("###subscribeClientConfigVoChange:configPath=" + configPath + ",dataKey=" + dataKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //客户端日志级别变更粒度到客户端pool级别
    private void updateHedwigClientLogLevel(HedwigClientConfigVo configVo, boolean needReset) {
        HedwigClientConfigDataVo dataVo = configVo.getConfigDataVo(HedwigClientConfigConstants.DEFAULT_ALL_SERVICE_NAME,
                HedwigClientConfigConstants.DEFAULT_ALL_METHOD_NAME);
        if (dataVo!=null&&StringUtils.isNotBlank(dataVo.getLogPackageName()) && StringUtils.isNotBlank(dataVo.getLogLevel())) {
            if (needReset) {
                LogManagerUtil.updatePackageLogLevel(dataVo.getLogPackageName(), LogManagerUtil.Level_ERROR);
            } else {
                LogManagerUtil.updatePackageLogLevel(dataVo.getLogPackageName(), dataVo.getLogLevel());
            }
        }
    }

    public String getClientPoolId() {
        return clientPoolId;
    }

    public String getProviderPoolId() {
        return providerPoolId;
    }

    @Override
    public String toString() {
        return "HedwigClientConfigLoader{" +
                "providerPoolId='" + providerPoolId + '\'' +
                ", clientPoolId='" + clientPoolId + '\'' +
                '}';
    }
}
