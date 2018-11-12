/**
 * 
 */
package com.yihaodian.architecture.hedwig.consumer;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.BaseProfile;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author root
 * 
 */
public class TestUtil {
	public static Logger logger = LoggerFactory.getLogger(TestUtil.class);

	public static String getLocalhostIp() {
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			logger.debug("Get host IP failed!!!");
		}
		return ip;
	}

	public static BaseProfile getBaseProfile() {
		BaseProfile bp = new BaseProfile();
		bp.setServiceAppName("testApp");
		bp.setServiceName("testService");
		bp.setServiceVersion("1.0");
		return bp;
	}

	public static ClientProfile getClientProfile() {
		ClientProfile bp = new ClientProfile();
		bp.setServiceAppName("testApp");
		bp.setServiceName("testService");
		bp.setServiceVersion("1.0");
		bp.setBalanceAlgo(InternalConstants.BALANCER_NAME_ROUNDROBIN);
		return bp;
	}

	public static ServiceProfile getServiceProfile() {
		ServiceProfile bp = new ServiceProfile();
		bp.setServiceAppName("testApp");
		bp.setServiceName("testService");
		bp.setServiceVersion("1.0");
		return bp;
	}
}
