/**
 * 
 */
package com.yhd.arch.laserbeak.client;

import java.util.Date;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.container.RootContainer;
import com.yhd.arch.laserbeak.client.util.MonitorLogUtil;
import com.yhd.arch.photon.codec.CodecCenter;
import com.yhd.arch.photon.codec.TransDataSerializer;
import com.yhd.arch.photon.constants.Constants;
import com.yhd.arch.photon.constants.PhotonStatus;
import com.yhd.arch.photon.core.MethodActorManager;
import com.yhd.arch.photon.core.PackagedMessage;
import com.yhd.arch.photon.core.RemoteResponse;
import com.yhd.arch.photon.core.ResponseFactory;
import com.yhd.arch.photon.core.ServiceInfo;
import com.yhd.arch.photon.core.TransData;
import com.yhd.arch.photon.emitter.event.change.MessageRetryEvent;
import com.yhd.arch.photon.emitter.router.RouteeRef;
import com.yhd.arch.photon.emitter.router.RouteeWraper;
import com.yhd.arch.photon.exception.RemoteExceptionFactory;
import com.yhd.arch.photon.invoker.DefaultRequest;
import com.yhd.arch.photon.plugin.IThrottler;
import com.yhd.arch.photon.util.ActorNameUtil;
import com.yhd.arch.photon.util.ClientNettySystem;
import com.yhd.arch.photon.util.ClientSystem;
import com.yihaodian.architecture.hedwig.client.util.HedwigMonitorClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.util.InvocationContext;
import com.yihaodian.monitor.dto.ClientBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import com.yihaodian.monitor.util.MonitorJmsSendUtil;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import scala.concurrent.Promise;
import scala.concurrent.duration.Duration;

/**
 * @author root
 *
 */
public class LogedAckActor extends UntypedActor {

	private Logger logger = LoggerFactory.getLogger(LogedAckActor.class);
	private Promise<RemoteResponse> _promise;
	private PackagedMessage _msg;
	private DefaultRequest<InvocationContext> _request;
	private ServiceInfo _info;
	private int _seq = 0;
	private IThrottler _throttler;

	public LogedAckActor(PackagedMessage message, Duration duration, IThrottler methodThrottler) {
		this._msg = message;
		this._promise = this._msg.getPromise();
		this._request = (DefaultRequest<InvocationContext>) this._msg.getRequest();
		this._seq = _request.getSequence();
		this.getContext().setReceiveTimeout(duration);
		this._info = message.getRequest().getServiceInfo();
		this._throttler = methodThrottler;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		String sName = this._info.getMeta().getServiceName();
		String profileUUId = this._info.getMeta().getProfileUUId();
		ClientProfile cp = RootContainer.getInstance().getClientProfile(sName, profileUUId);
		ClientBizLog cbLog = MonitorLogUtil.createBizLog(_request, cp);
		cbLog.setCommId(_seq + "");
		if (message instanceof byte[]) {
			// System.out.println("LogedAckActor reqId:" + _request.getReqId() +
			// ",time:" + System.currentTimeMillis());
			byte[] bytes = (byte[]) message;
			TransData data = (TransData) TransDataSerializer.getInstance().fromBinaryJava(bytes);
			try {
				long dserStart = 0;
				if (logger.isDebugEnabled()) {
					dserStart = System.currentTimeMillis();
				}
				RemoteResponse response = CodecCenter.getInstance().responseDecode(data.getBody(), data.getCodecType());
				if (logger.isDebugEnabled()) {
					long dserEnd = System.currentTimeMillis();
					logger.debug("Response deserialize cost:" + (dserEnd - dserStart) + "ms");
				}
				processResponse(response, cbLog);
			} catch (Throwable e) {
				logger.error("Response deserialize error", e);
				finishByException(e);
			}
		} else if (message instanceof RemoteResponse) {
			RemoteResponse response = (RemoteResponse) message;
			processResponse(response, cbLog);
		} else if (message.equals(ReceiveTimeout.getInstance())) {
			Channel channel = _msg.getSenderChannel();
			if (channel != null) {
				ClientNettySystem.getInstance().getSyncConnPool().destory(channel);
			}
			RouteeRef routeeRef = _request.getRoutee(_seq);
			RouteeWraper routee = routeeRef != null ? routeeRef.getRoutee() : null;
			if (routee != null) {
				PhotonStatus status = routeeRef.getStatus();
				if (PhotonStatus.ENABLE.equals(status)) {
					routee.setTmpDisable();
					logger.error("Request read timeout on:" + routee.getHostUrl() + ",kick out node and msg going to be retry");
				}
			} else {
				logger.error("Request don't assigned a remote host yet,sequence:" + _seq);
			}
			HedwigMonitorClientUtil.setException(cbLog, RemoteExceptionFactory.createRequestTimeoutException(_request));
			cbLog.setInParamObjects(_request.getParameters());
			retryMessage(cbLog);
		} else if (message instanceof MessageRetryEvent) {
			if (!message.equals(MessageRetryEvent.Unconditional_Retry)) {
				MessageRetryEvent retryEvent = (MessageRetryEvent) message;
				Throwable t = retryEvent.getCaseError();
				HedwigMonitorClientUtil.setException(cbLog, t);
			}
			retryMessage(cbLog);
		} else {
			unhandled(message);
		}
		getContext().stop(getSelf());
	}

	private void processResponse(RemoteResponse response, ClientBizLog cbLog) {
		if (isOutstanding()) {
			RouteeRef routeeRef = _request.getRoutee(_seq);
			RouteeWraper routee = routeeRef != null ? routeeRef.getRoutee() : null;
			try {
				if (response.getMessageType() == Constants.MESSAGE_TYPE_SUCCESS) {
					finishByResponse(response);
				} else {
					Throwable t = response.getCause();
					finishByException(t);
					cbLog.setRespTime(new Date());
					String hostUrl = routee != null ? routee.getHostUrl() : "";
					if(routee!=null){
						cbLog.setProviderLevel(routee.getLevel());
						cbLog.setProviderIDC(routee.getIdc());
						cbLog.setProviderZone(routee.getZone());
					}
					cbLog.setProviderHost(hostUrl);
					cbLog.setMemo(cbLog.getMemo() + ",hedwigVersion:laserbeak");
					HedwigMonitorClientUtil.setException(cbLog, t);
					cbLog.setLayerType(MonitorConstants.LAYER_TYPE_HANDLER);
					cbLog.setCurtLayer(_request.getContext().getHopValue());
					cbLog.setLocalLayer(System.nanoTime());
					cbLog.setCommId(_seq + "");
					MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
				}
				if (routee != null) {
					PhotonStatus status = routeeRef.getStatus();
					if (PhotonStatus.TEMPORARY_DISABLE.equals(status)) {
						routee.setEnableStatus();
						logger.warn(InternalConstants.LOG_PROFIX + " routee relive!! routee=" + routee.getHostUrl());
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				finishByException(e);
			}

		}

	}

	private void retryMessage(ClientBizLog cbLog) {
		RouteeWraper routee = null;
		try {
			RouteeRef routeeRef = _request.getRoutee(_seq);
			routee = routeeRef != null ? routeeRef.getRoutee() : null;
			Throwable t = null;
			if (_msg.isRetryAble()) {
				_msg.setSenderChannel(null);
				String key = ActorNameUtil.generateUniqKey(_request.getMethodName(), _request.getProfileUUId());
				ActorRef mActor = MethodActorManager.getInStance().selectActor(key);
				if (mActor != null) {
					_request.increaseSeq();

					/*
					 * Updated by Frank wang on Nov 30, 2106.
					 * 
					 * When a hedwig request fails, it may trigger retrying policy.
					 * However, if the createing time stamp of this request has not been updated,
					 * MethodActor.isNeedProcess() may return false because of this request was time out even though
					 * it's not been handled at all. 
					 * 
					 * That's why we could see the exception "pending queue...".
					 */
					_request.createMillisTime();
					mActor.tell(this._msg, ActorRef.noSender());
				} else {
					if (isOutstanding()) {
						t = RemoteExceptionFactory.createNoRouterException(_request);
						finishByException(t);
					}
				}
			} else {
				if (isOutstanding()) {
					t = RemoteExceptionFactory.createRequestTimeoutException(_request);
					finishByException(t);
				}
			}
		} finally {
			cbLog.setRespTime(new Date());
			String hostUrl = routee != null ? routee.getHostUrl() : "";
			cbLog.setProviderHost(hostUrl);
			cbLog.setLayerType(MonitorConstants.LAYER_TYPE_HANDLER);
			cbLog.setCurtLayer(_request.getContext().getHopValue());
			cbLog.setLocalLayer(System.nanoTime());
			cbLog.setCommId(_seq + "");
			MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
		}
	}

	private void finishByException(Throwable t) {
		this._promise.failure(t);
		RemoteResponse rr = ResponseFactory.createServiceExceptionResponse(t, _seq);
		this._throttler.releaseRequest(this._request, rr);
	}

	private void finishByResponse(RemoteResponse response) {
		this._promise.success(response);
		this._throttler.releaseRequest(this._request, response);
	}

	private boolean isOutstanding() {
		return !this._promise.isCompleted();
	}

	@Override
	public void postStop() throws Exception {
		ClientSystem.getInstance().getActorSystem().eventStream().unsubscribe(getSelf());
		super.postStop();
	}

}
