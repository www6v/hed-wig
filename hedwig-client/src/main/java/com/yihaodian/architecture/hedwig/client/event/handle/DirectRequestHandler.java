/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event.handle;

import org.aopalliance.intercept.MethodInvocation;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.architecture.hedwig.engine.exception.HandlerException;

/**
 * @author root
 *
 */
public class DirectRequestHandler extends BaseHandler {

	@Override
	protected Object doHandle(HedwigContext context, BaseEvent event) throws HandlerException {
		Object hessianProxy = null;
		Object result = null;
		String sUrl = context.getClientProfile().getTarget();
		String reqId = event.getReqestId();
		if (HedwigUtil.isBlankString(sUrl)) {
			throw new HandlerException(reqId, "Target url must not null!!!");
		}
		String host = HedwigUtil.getHostFromUrl(sUrl);
		event.setTryHost(host);
		try {
			hessianProxy = HedwigClientUtil.getHessianProxy(context, sUrl);
		} catch (Exception e) {
			throw new HandlerException(reqId, e.getCause());
		}

		if (hessianProxy == null) {
			context.getHessianProxyMap().remove(sUrl);
			throw new HandlerException(reqId, "HedwigHessianInterceptor is not properly initialized");
		}
		try {
			MethodInvocation invocation = event.getInvocation();
			result = invocation.getMethod().invoke(hessianProxy, invocation.getArguments());
			event.setState(EventState.sucess);
		} catch (Throwable e) {
			event.setState(EventState.failed);
			event.setRemoteException(e.getCause());
			throw new HandlerException(reqId, e.getCause());
		}
		return result;
	}

}
