package com.yihaodian.architecture.hedwig.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigJsonUtil {
    public static final String toJSONString(Object object) {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(ServiceProfile.class, "servicePath"
                ,"serviceUrl","protocolPrefix","urlPattern","hostIp","jvmPid","port","revision","weighted"
                ,"loadRate","loadThreshold","curWeight","curStatus","relivePolicy","registTime"
                ,"assembleAppName","pubZone","regZone","pubPoolName","methodRP","mehodNames");
        return JSON.toJSONString(object,filter);
    }
}
