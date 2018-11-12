/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event;

import java.util.Set;

import com.yihaodian.architecture.hedwig.common.util.HedwigTimeoutUtil;
import org.aopalliance.intercept.MethodInvocation;

import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.RequestType;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.common.KeyUtil;

/**
 * @author root
 * 
 */
public class HedwigEventBuilder {

	private HedwigContext context;
	private ClientProfile clientProfile;
	private Set<String> noRetoryMethods;

	public HedwigEventBuilder(HedwigContext context, ClientProfile clientProfile) {
		super();
		this.context = context;
		this.clientProfile = clientProfile;
		this.noRetoryMethods = clientProfile.getNoRetryMethods();
	}

	public BaseEvent buildRequestEvent(MethodInvocation invocation) {
		BaseEvent event = null;
		long expire = clientProfile.getTimeout();
		if (!HedwigUtil.isBlankString(clientProfile.getTarget())) {
			event = directRequestEvent(invocation);
			event.setRequestType(RequestType.SyncInner);
		} else if (clientProfile.isUseBroadcast()) {
			event = broadcastEvent(invocation);
			event.setRequestType(RequestType.getByName(clientProfile.getRequestType()));
		} else {
			event = syncRequestEvent(invocation);
			event.setRequestType(RequestType.getByName(clientProfile.getRequestType()));
		}
		if (expire < InternalConstants.DEFAULT_REQUEST_TIMEOUT) {
			expire = expire << 1;
		}
		Long reqTimeout= HedwigTimeoutUtil.getRequestTimeout();
		if(reqTimeout!=null&&reqTimeout>0L) {
			event.setExpireTime(reqTimeout);
		}else{
			event.setExpireTime(expire);
		}
		String reqId = KeyUtil.getReqId(clientProfile.getServiceAppName(), event.getRequestTime());
		event.setReqestId(reqId);
		return event;
	}

	private BaseEvent broadcastEvent(MethodInvocation invocation) {
		BroadcastEvent event = new BroadcastEvent(invocation);
		event.setRetryable(false);
		event.setState(EventState.init);
		return event;
	}

	private SyncRequestEvent syncRequestEvent(MethodInvocation invocation) {
		SyncRequestEvent event = new SyncRequestEvent(invocation);
		String methodName = invocation.getMethod().getName();
		if (clientProfile.isRedoAble()) {
			if (noRetoryMethods == null || !noRetoryMethods.contains(methodName)) {
				event.setMaxRedoCount(HedwigClientUtil.getRedoCount(context));
				event.setRetryable(true);
			}
		}
		event.setState(EventState.init);
		return event;
	}

	private DirectRequestEvent directRequestEvent(MethodInvocation invocation) {
		DirectRequestEvent event = new DirectRequestEvent(invocation);
		event.setState(EventState.init);
		return event;
	}

}
