/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.event.handle;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.client.util.HedwigMonitorClientUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.architecture.hedwig.engine.handler.IEventHandler;
import com.yihaodian.monitor.dto.ClientBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import com.yihaodian.monitor.util.MonitorJmsSendUtil;

/**
 * @author root
 * @param <C>
 * 
 */
public abstract class BaseHandler implements IEventHandler<HedwigContext, BaseEvent, Object> {

	private Logger logger = LoggerFactory.getLogger(BaseHandler.class);

	@Override
	public Object handle(HedwigContext context, BaseEvent event) throws Throwable {
		event.increaseExecCount();
		ClientBizLog cbLog = null;
		String globalId = HedwigContextUtil.getGlobalId();
		String txnId = HedwigClientUtil.generateTransactionId();
		String reqId = event.getReqestId();
		HedwigContextUtil.setTransactionId(txnId);
		cbLog = HedwigMonitorClientUtil.createClientBizLog(event, context, reqId, globalId, new Date());
		cbLog.setCommId(txnId);
		Object r = null;
		Object[] params = event.getInvocation().getArguments();
		try {
			r = doHandle(context, event);
			event.setState(EventState.sucess);
			event.setResult(r);
			cbLog.setRespTime(new Date());
			cbLog.setSuccessed(MonitorConstants.SUCCESS);
		} catch (Throwable e) {
			event.setState(EventState.failed);
			event.setErrorMessage(e.getMessage());
			cbLog.setInParamObjects(params);
			HedwigMonitorClientUtil.setException(cbLog, e);
			throw e;
		} finally {
			//
			cbLog.setProviderZone(event.getProviderZone());
			cbLog.setProviderIDC(event.getProviderIDC());
			cbLog.setProviderLevel(event.getProviderLevel());

			cbLog.setProviderHost(event.getLastTryHost());
			cbLog.setCurtLayer(HedwigContextUtil.getRequestHop());
            cbLog.setLocalLayer(System.nanoTime());
			if (MonitorConstants.FAIL == cbLog.getSuccessed()) {
				cbLog.setLayerType(MonitorConstants.LAYER_TYPE_HANDLER);
				MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
			}
		}

		return r;
	}

	abstract protected Object doHandle(HedwigContext context, BaseEvent event) throws Throwable;

}
