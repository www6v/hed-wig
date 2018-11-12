package com.yhd.arch.laserbeak.client.meta;

import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigClientConfigVo implements Serializable {
    private static final long serialVersionUID = -3449736799079134999L;
    private static Logger logger = LoggerFactory.getLogger(HedwigClientConfigVo.class);
    private Map<String, HedwigClientConfigDataVo> configDataMap = new HashMap<String, HedwigClientConfigDataVo>();

    private static String buildConfigDataKey(String serviceName, String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceName).append("@").append(methodName);
        return sb.toString();
    }

    public void addConfigDataVo(HedwigClientConfigDataVo dataVo) {
        String key = buildConfigDataKey(dataVo.getServiceName(), dataVo.getMethodName());
        //根据权限认证用户名和密码设置生成code
        if (!HedwigUtil.isBlankString(dataVo.getUserName()) && !HedwigUtil.isBlankString(dataVo.getPassword())) {
            dataVo.setSignCode(HedwigUtil.genAuthorization(dataVo.getUserName(), dataVo.getPassword()));
        }
        configDataMap.put(key, dataVo);
    }

    public void removeConfigDataVo(String serviceName, String methodName) {
        String key = HedwigClientConfigVo.buildConfigDataKey(serviceName, methodName);
        configDataMap.remove(key);
    }
    public HedwigClientConfigDataVo getConfigDataVo(String serviceName, String methodName) {
        String key = HedwigClientConfigVo.buildConfigDataKey(serviceName, methodName);
        return configDataMap.get(key);
    }

    /**
     * 通过级别获取获取相应配置
     * @param serviceName
     * @param methodName
     * @return
     */
    public HedwigClientConfigDataVo getConfigDataVoByLevel(String serviceName, String methodName) {
        HedwigClientConfigDataVo resultVo = null;
        if(StringUtils.isNotBlank(serviceName)&&StringUtils.isNotBlank(methodName)) {
            //优先读取这个接口这个方法配置
            String key = HedwigClientConfigVo.buildConfigDataKey(serviceName, methodName);
            HedwigClientConfigDataVo dataVo = configDataMap.get(key);

            //如果之前没读到，再读取这个接口所有方法默认配置
            if (dataVo == null||dataVo.getDataStatus()!=HedwigClientConfigConstants.CONFIG_DATA_STATUS_AUDITED) {
                key = HedwigClientConfigVo.buildConfigDataKey(serviceName, HedwigClientConfigConstants.DEFAULT_ALL_METHOD_NAME);
                dataVo = configDataMap.get(key);
            }
            //如果之前没读到，再读取这个服务pool所有接口所有方法默认配置
            if (dataVo == null||dataVo.getDataStatus()!=HedwigClientConfigConstants.CONFIG_DATA_STATUS_AUDITED) {
                key = HedwigClientConfigVo.buildConfigDataKey(HedwigClientConfigConstants.DEFAULT_ALL_SERVICE_NAME, HedwigClientConfigConstants.DEFAULT_ALL_METHOD_NAME);
                dataVo = configDataMap.get(key);
            }
            if(dataVo!=null&&dataVo.getDataStatus()==HedwigClientConfigConstants.CONFIG_DATA_STATUS_AUDITED){
                resultVo=dataVo;
            }
        }else{
            logger.error("###serviceName="+serviceName+",methodName="+methodName);
        }
        return resultVo;
    }

    public Map<String, HedwigClientConfigDataVo> getConfigDataMap() {
        return configDataMap;
    }

    public void setConfigDataMap(Map<String, HedwigClientConfigDataVo> configDataMap) {
        this.configDataMap = configDataMap;
    }
}
