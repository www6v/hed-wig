/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event.handle;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

/**
 * @author root
 *
 */
public class HandlerUtil {

	public static Set<String> NETWORK_EXCEPTIONS = null;
	
	static{
		NETWORK_EXCEPTIONS = new HashSet<String>();
		NETWORK_EXCEPTIONS.add(ConnectException.class.getName());
		NETWORK_EXCEPTIONS.add(SocketException.class.getName());
		NETWORK_EXCEPTIONS.add(IOException.class.getName());
		NETWORK_EXCEPTIONS.add(java.net.SocketTimeoutException.class.getName());
	}
	
	public static boolean isNetworkException(Throwable exception) {
		boolean value = false;
		String rootCause = HedwigMonitorUtil.getExceptionClassName(exception);
		if (!HedwigUtil.isBlankString(rootCause)) {
			value = NETWORK_EXCEPTIONS.contains(rootCause);
		}
		return value;
	}
}
