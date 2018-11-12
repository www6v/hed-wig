/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event.handle;

import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.ServiceStatus;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author root
 * 
 */
public class BroadcastHandler extends BaseHandler {

	Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class);

	@Override
	protected Object doHandle(HedwigContext context, BaseEvent event) throws Throwable {
		Object result = null;
		Object obj = null;
		Set<String> sendHosts = new HashSet<String>();
		for (int i = 0; i < 100; i++) {
			ServiceProfile sp = context.getLocator().getService();
			if (sp != null) {
				String sUrl = sp.getServiceUrl();
				String hostString = sp.getHostString();
				if (sendHosts.contains(hostString)) {
					break;
				}
				sendHosts.add(hostString);
				Object hessianProxy = null;
				try {
					hessianProxy = HedwigClientUtil.getHessianProxy(context, sUrl);
				} catch (Exception e) {
					event.setErrorMessage(hostString + "::" + e.getMessage());
				}
				if (hessianProxy != null) {
					MethodInvocation invocation = event.getInvocation();
					Object[] params = invocation.getArguments();
					try {
						obj = invocation.getMethod().invoke(hessianProxy, params);
						if(obj!=null){
							result = obj;
						}
						if (sp.getCurStatus().equals(ServiceStatus.TEMPORARY_DISENABLE)) {
							sp.setCurStatus(ServiceStatus.ENABLE);
						}
					} catch (Throwable e) {
						event.setRemoteException(e.getCause());
						if (HandlerUtil.isNetworkException(e) && context.getClientProfile().isClientThrottle()
								&& !sp.getCurStatus().equals(ServiceStatus.TEMPORARY_DISENABLE)) {
							sp.setCurStatus(ServiceStatus.TEMPORARY_DISENABLE);
							logger.info(InternalConstants.LOG_PROFIX + sp.getHostString() + " has kickout of candidate!!!");
						}
					}
				}
			}
		}
		return result;
	}
}
