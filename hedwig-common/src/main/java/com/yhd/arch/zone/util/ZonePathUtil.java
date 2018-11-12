/**
 * 
 */
package com.yhd.arch.zone.util;

import com.yhd.arch.zone.ZoneConstants;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 *
 */
public class ZonePathUtil {

	public static String getCrossZoneServicePath(ServiceProfile profile) {
		String oriAppcode = YccGlobalPropertyConfigurer.getMainPoolId();
		oriAppcode = HedwigUtil.isBlankString(oriAppcode) ? profile.getServiceAppName() : oriAppcode;
		String appcode = StringUtils.replaceSlash(oriAppcode);
		return getCrossZoneServicePath(appcode, profile.getServiceName());
	}

	public static String getCrossZoneServicePath(String poolName, String serviceName) {
		StringBuilder pathBuilder = new StringBuilder(getCrossZonePoolPath(poolName));
		pathBuilder.append("/").append(serviceName);
		return pathBuilder.toString();
	}

	public static String getCrossZonePoolPath(String poolName) {
		StringBuilder pathBuilder = new StringBuilder(InternalConstants.BASE_ROOT_FLAGS);
		pathBuilder.append("/").append(poolName).append(ZoneConstants.FLAG_CROSS_ZONE);
		return pathBuilder.toString();
	}

	public static String getCrossZonePoolPath(ServiceProfile profile) {
		String oriAppcode = YccGlobalPropertyConfigurer.getMainPoolId();
		oriAppcode = HedwigUtil.isBlankString(oriAppcode) ? profile.getServiceAppName() : oriAppcode;
		String appcode = StringUtils.replaceSlash(oriAppcode);
		return getCrossZonePoolPath(appcode);
	}
}
