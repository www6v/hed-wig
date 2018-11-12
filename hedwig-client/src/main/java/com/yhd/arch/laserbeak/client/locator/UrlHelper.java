/**
 * 
 */
package com.yhd.arch.laserbeak.client.locator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.laserbeak.common.util.RoutePriorityUtil;
import com.yhd.arch.photon.constants.ProtocolType;
import com.yhd.arch.photon.constants.SupportedCodec;
import com.yhd.arch.photon.util.ActorNameUtil;
import com.yihaodian.architecture.hedwig.common.constants.ServiceStatus;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

/**
 * @author root
 * 
 */
public class UrlHelper {

	private static Logger logger = LoggerFactory.getLogger(UrlHelper.class);

	public static ServiceProfile genServiceProfileFromStringUrl(String strUrl, String serviceName, Class clazz) {
		ServiceProfile sp = null;
		if (!HedwigUtil.isBlankString(strUrl)) {
			if (strUrl.startsWith(ProtocolType.HTTP.getPrefix())) {
				sp = getSpFromHttpUrl(strUrl, serviceName);
			} else if (strUrl.startsWith(ProtocolType.AKKAtcp.getPrefix())) {
				sp = getSpFromAkkaUrl(strUrl, serviceName);
			}
			if (sp != null) {
				List<String> mList = ActorNameUtil.getMethodNames(clazz);
				sp.setMehodNames(mList);
				sp.setMethodRP(RoutePriorityUtil.createDefaultMethodRP(mList));
			}
		}
		return sp;
	}

	// akka.tcp://192.168.112.126:20909
	private static ServiceProfile getSpFromAkkaUrl(String strUrl, String serviceName) {
		ServiceProfile sp = null;
		String hostStr = getHostStr(strUrl);
		if (!HedwigUtil.isBlankString(hostStr)) {
			String[] arr = hostStr.split(":");
			if (arr != null && arr.length == 2) {
				sp = new ServiceProfile();
				sp.setHostIp(arr[0]);
				sp.setPort(Integer.valueOf(arr[1]));
				sp.setCurStatus(ServiceStatus.ENABLE);
				sp.setTransProtocol(ProtocolType.AKKAtcp);
				sp.setWeighted(1);
				sp.setCodecName(SupportedCodec.HEDWIG);
				sp.setServiceUrl(strUrl);
				sp.setServiceName(serviceName);
			} else {
				logger.error("Can't generate ServiceProfile from target:" + strUrl);
			}
		}
		return sp;
	}

	private static String getHostStr(String strUrl) {
		String hostStr = strUrl;
		if (strUrl.contains(ProtocolType.AKKAtcp.getPrefix())) {
			if (strUrl.contains("@")) {
				String[] arr = strUrl.split("@");
				if (arr != null && arr.length == 2) {
					String origin = arr[1];
					if (!HedwigUtil.isBlankString(origin)) {
						if (origin.contains("/")) {
							hostStr = origin.substring(0, origin.indexOf("/"));
						}
					}
				}
			} else {
				hostStr = strUrl.replaceAll("akka.tcp://", "");
			}
		}
		return hostStr;
	}

	private static ServiceProfile getSpFromHttpUrl(String strUrl, String serviceName) {
		ServiceProfile sp = null;
		try {
			URL url = new URL(strUrl);
			sp = new ServiceProfile();
			sp.setHostIp(url.getHost());
			sp.setPort(url.getPort());
			sp.setCurStatus(ServiceStatus.ENABLE);
			sp.setTransProtocol(ProtocolType.HTTP);
			sp.setWeighted(1);
			sp.setCodecName(SupportedCodec.HEDWIG);
			sp.setServiceUrl(strUrl);
			sp.setServiceName(serviceName);
		} catch (MalformedURLException e) {
			logger.error("Can't generate ServiceProfile from target:" + strUrl, e);
		}
		return sp;
	}
}
