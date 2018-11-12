package com.yihaodian.architecture.hedwig.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigConfigUtil {
    private static Logger logger = LoggerFactory.getLogger(HedwigConfigUtil.class);
    private static final String HEDWIG_INVOKE_GROUP_NAME = "HEDWIG_INVOKE_GROUP_NAME";
    /**
     * 设置方法这一次请求调用超时时间 单位ms
     *
     * @param timeout     timeout是一次服务调用的最长等待时间，对应代码为Future.get(timeout)；单位ms
     * @param readTimeout readTimeout是远程请求所使用的链接的最长等待时间，超过这个时间链接会自动断开。
     *                    readTimeout的值为服务端处理请求的时间+网络传输时间；timeout的值根据准许的重试次数可以设置为readtiemout的3-5倍
     */
    public static void setRequestTimeout(Long timeout, Long readTimeout) {
        HedwigTimeoutUtil.setRequestTimeout(timeout, readTimeout);
    }

    public static Long getRequestTimeout() {
        return HedwigTimeoutUtil.getRequestTimeout();
    }

    public static Long getRequestReadTimeout() {
        return HedwigTimeoutUtil.getRequestReadTimeout();
    }
    /**
     * 设置方法这一次请求所属分组名称
     *
     * @param groupName  分组名称
     */
    public static void setRequestGroupName(String groupName) {
        if (org.apache.commons.lang.StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("### Hedwig groupName Config ERROR!!  " +
                    "groupName=" + groupName);
        } else {
            HedwigContextUtil.setAttribute(HEDWIG_INVOKE_GROUP_NAME, groupName);
        }
    }

    public static String getRequestGroupName() {
        String result = null;
        Object obj = HedwigContextUtil.getAttribute(HEDWIG_INVOKE_GROUP_NAME, null);
        if (obj != null) {
            result = (String) obj;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("###-----getRequestGroupName()=" + result + "-----");
        }
        return result;
    }
}
