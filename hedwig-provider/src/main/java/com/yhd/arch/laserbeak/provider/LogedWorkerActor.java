package com.yhd.arch.laserbeak.provider;

import java.lang.reflect.Method;
import java.util.Date;

import com.yhd.arch.zone.ZoneContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.container.RootContainer;
import com.yhd.arch.photon.codec.CodecCenter;
import com.yhd.arch.photon.codec.TransDataSerializer;
import com.yhd.arch.photon.constants.Constants;
import com.yhd.arch.photon.constants.PhotonPropKeys;
import com.yhd.arch.photon.core.RemoteRequest;
import com.yhd.arch.photon.core.RemoteResponse;
import com.yhd.arch.photon.core.ResponseFactory;
import com.yhd.arch.photon.core.TransData;
import com.yhd.arch.photon.entrypoint.EndpointMethod;
import com.yhd.arch.photon.entrypoint.EndpointRepository;
import com.yhd.arch.photon.event.status.ActorInitEvent;
import com.yhd.arch.photon.exception.PhotonException;
import com.yhd.arch.photon.exception.PhotonSecurityException;
import com.yhd.arch.photon.exception.TimeoutException;
import com.yhd.arch.photon.invoker.DefaultRequest;
import com.yhd.arch.photon.plugin.IThrottler;
import com.yhd.arch.photon.util.ActorNameUtil;
import com.yhd.arch.photon.util.PhotonUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.common.util.InvocationContext;
import com.yihaodian.monitor.dto.ServerBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import com.yihaodian.monitor.util.MonitorJmsSendUtil;

import akka.actor.UntypedActor;

public class LogedWorkerActor extends UntypedActor {

	private Logger logger = LoggerFactory.getLogger(LogedWorkerActor.class);

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof RemoteRequest) {
			ServerBizLog sbLog = new ServerBizLog();
			RemoteResponse response = null;
			DefaultRequest<InvocationContext> request = (DefaultRequest<InvocationContext>) message;
			Object o = request.getContext().getValue(PhotonPropKeys.KEY_ENDPOINT_THROTTLER, null);
			request.getContext().removeValue(PhotonPropKeys.KEY_ENDPOINT_THROTTLER);
			request.getContext().increaseHopValue();// HEDWIG_REQUEST_HOP 层次加1
			String ct = PhotonUtil.getCodecType(request, true);
			boolean lazyTrigger = PhotonUtil.isLazyTrigger(request, true);
			HedwigContextUtil.setInvocationContext(request.getContext());
			ServiceProfile sp = null;
			long start = request.getCreateMillisTime();
			Date reqTime = new Date(start);
			try {
				sbLog.setGetReqTime(reqTime);
				sbLog.setProviderApp(RootContainer.getInstance().getAppName());
				String sName = ActorNameUtil.extractServiceName(request.getMethodName());
				sp = RootContainer.getInstance().getServiceProfile(sName, request.getProfileUUId());
				sbLog.setProviderHost(sp != null ? sp.getHostString() : "UnknowHost");
				sbLog.setServiceName(sName);
				long currentTime = System.currentTimeMillis();
				if (start + request.getReadTimeout() < currentTime) {
					String toMsg = createTimeoutBeforeBiz(request);
					TimeoutException te = new TimeoutException(ResponseFactory.appendSequence(toMsg, request.getSequence()));
					logger.error(te.getMessage(), te);
					throw te;
				} else {
					long bizStart = 0;
					if (logger.isDebugEnabled()) {
						bizStart = System.currentTimeMillis();
					}

					/*
					 * Updated by Frank Wang on Dec 7th, 2016
					 * 
					 * Prepare parent id for the next SOA invocation.
					 */

					String parentId = HedwigContextUtil.getRequestId();
					HedwigContextUtil.getInvocationContext().putValue(InternalConstants.HEDWIG_REQUEST_PARENT_ID, parentId);

					response = doBusiness(request);
					if (logger.isDebugEnabled()) {
						long bizEnd = System.currentTimeMillis();
						logger.debug("Do business cost:" + (bizEnd - bizStart) + "ms");
					}
					// System.out.println("LogedWorkerActor reqId:" +
					// request.getReqId() + ",time:" +
					// System.currentTimeMillis());
					// 增加业务处理完成后是否超时判断，如果超时抛出异常
					currentTime = System.currentTimeMillis();
					if (start + request.getReadTimeout() < currentTime) {
						String toMsg = createTimeoutAfterBiz(request);
						TimeoutException te = new TimeoutException(ResponseFactory.appendSequence(toMsg, request.getSequence()));
						logger.error(te.getMessage(), te);
						throw te;
					} else {
						sbLog.setSuccessed(MonitorConstants.SUCCESS);
					}
				}
			} catch (Throwable t) {
				sbLog.setSuccessed(MonitorConstants.FAIL);
				sbLog.setInParamObjects(request.getParameters());
				sbLog.setExceptionClassname(HedwigMonitorUtil.getExceptionClassName(t));
				sbLog.setExceptionDesc(HedwigMonitorUtil.getExceptionMsg(t));
				if (request.getCallType() == Constants.CALLTYPE_REPLY) {
					response = doFailResponse(request, t);
				}
			} finally {
				if (response != null) {
					if (lazyTrigger) {
						long serStart = 0;
						if (logger.isDebugEnabled()) {
							serStart = System.currentTimeMillis();
						}
						byte[] bytes = CodecCenter.getInstance().responseEncode(response, ct);
						if (logger.isDebugEnabled()) {
							long serEnd = System.currentTimeMillis();
							logger.debug("Response serialize cost:" + (serEnd - serStart) + "ms");
						}
						TransData td = new TransData(ct, bytes);
						getSender().tell(TransDataSerializer.getInstance().toBinary(td), getSelf());
					} else {
						getSender().tell(response, getSelf());
					}
				}
				//
				ZoneContainer zoneContainer=ZoneContainer.getInstance();
				sbLog.setProviderIDC(zoneContainer.getIdcContainer().getLocalIdc());
				sbLog.setProviderZone(zoneContainer.getLocalZoneName());
				sbLog.setProviderLevel(zoneContainer.getLevel().getCode());

				sbLog.setUniqReqId(request.getGlobalId());
				sbLog.setReqId(request.getReqId());
				sbLog.setCommId(request.getSequence() + "");
				String groups = (request.getGroupNames() == null || request.getGroupNames().size() == 0) ? InternalConstants.NON_GROUP
						: request.getGroupNames().toString();
				sbLog.setServiceGroup(groups);
				sbLog.setReqTime(reqTime);
				sbLog.setRespResultTime(new Date());
				sbLog.setMethodName(request.getMethodName());
				sbLog.setServiceVersion(sp != null ? sp.getServiceVersion() : "UnknowVersion");
				MonitorJmsSendUtil.asyncSendServerBizLog(sbLog);
				if (o != null) {
					IThrottler throttler = (IThrottler) o;
					throttler.releaseRequest(request, response);
				}
				HedwigContextUtil.cleanGlobal();
			}

		} else if (message instanceof ActorInitEvent) {
			logger.debug(getSelf().toString() + " has initialed!!!");
		}
	}

	private String createTimeoutBeforeBiz(RemoteRequest request) {
		// TimeOut Exception
		StringBuffer msg = new StringBuffer(InternalConstants.LASER_SERVER_LOG_PROFIX + "Request timeout before worker handle it, source:");
		msg.append(getSender().path().toSerializationFormat()).append(" request readTimeout:").append(request.getReadTimeout())
				.append("  createTime:").append(request.getCreateMillisTime()).append("\r\n");
		Object[] params = request.getParameters();
		if (params != null && params.length > 0) {
			for (Object param : params) {
				msg.append("param ").append(param.getClass()).append(":").append(String.valueOf(param));
			}
			msg.append("\r\n");
		}
		return msg.toString();
	}

	private String createTimeoutAfterBiz(RemoteRequest request) {
		// TimeOut Exception
		StringBuffer msg = new StringBuffer(InternalConstants.LASER_SERVER_LOG_PROFIX + "Request timeout after worker handle it, source:");
		msg.append(getSender().path().toSerializationFormat()).append(" request readTimeout:").append(request.getReadTimeout())
				.append("  createTime:").append(request.getCreateMillisTime()).append("\r\n");
		Object[] params = request.getParameters();
		if (params != null && params.length > 0) {
			for (Object param : params) {
				msg.append("param ").append(param.getClass()).append(":").append(String.valueOf(param));
			}
			msg.append("\r\n");
		}
		return msg.toString();
	}

	private RemoteResponse doBusiness(RemoteRequest request) throws Throwable {
		RemoteResponse response = null;
		EndpointMethod methodWraper = null;
		methodWraper = EndpointRepository.getInstance().getMethod(request.getMethodName(), request.getProfileUUId());
		if (methodWraper != null) {
			String sCode = methodWraper.getSecureCode();
			if (sCode != null) {
				if (!sCode.equals(request.getSecureCode())) {
					throw new PhotonSecurityException(ResponseFactory.appendSequence(
							InternalConstants.LASER_SERVER_LOG_PROFIX + "Unautherized User and Password!!!", request.getSequence()));
				}
			}
			Method realMethod = methodWraper.getMethod();
			if (realMethod != null) {
				Object returnObj = null;
				returnObj = realMethod.invoke(methodWraper.getEndpoint(), request.getParameters());
				if (request.getCallType() == Constants.CALLTYPE_REPLY) {
					response = ResponseFactory.createSuccessResponse(returnObj, request.getSequence());
				}
			} else {
				throw new PhotonException(ResponseFactory.appendSequence(
						InternalConstants.LASER_SERVER_LOG_PROFIX + "Can't find request method:" + request.getMethodName(),
						request.getSequence()));
			}
		} else {
			throw new PhotonException(ResponseFactory.appendSequence(
					InternalConstants.LASER_SERVER_LOG_PROFIX + "Can't find request method:" + request.getMethodName(),
					request.getSequence()));
		}
		return response;
	}

	private RemoteResponse doFailResponse(RemoteRequest request, Throwable e) {
		//		return ResponseFactory.createFailResponse(InternalConstants.LASER_SERVER_LOG_PROFIX + e.getClass().getName() + ":::"
		//				+ HedwigMonitorUtil.getExceptionMsg(e), request.getSequence());
		return ResponseFactory.createFailResponse(
				InternalConstants.LASER_SERVER_LOG_PROFIX + e.getClass().getName() + ":::" + HedwigMonitorUtil.getExceptionMsg(e),
				request.getSequence(), e);
	}
}


//
//Caused by: com.yhd.arch.photon.exception.PhotonException: Laser server said: java.lang.reflect.InvocationTargetException:::ErrorMsg:null
//ClassName:sun.reflect.GeneratedMethodAccessor35; MethodName:invoke; LineNumber:-1;
//java.lang.reflect.InvocationTargetException:null
//com.yhd.shareservice.exceptions.HedwigException:null
//,sequence:0
//        at com.yhd.arch.photon.core.ResponseFactory.createFailResponse(ResponseFactory.java:17)
//        at com.yhd.arch.laserbeak.provider.LogedWorkerActor.doFailResponse(LogedWorkerActor.java:205)
//        at com.yhd.arch.laserbeak.provider.LogedWorkerActor.onReceive(LogedWorkerActor.java:101)
//        at akka.actor.UntypedActor$$anonfun$receive$1.applyOrElse(UntypedActor.scala:167)
//        at akka.actor.Actor$class.aroundReceive(Actor.scala:465)
//        at akka.actor.UntypedActor.aroundReceive(UntypedActor.scala:97)
//        at akka.actor.ActorCell.receiveMessage(ActorCell.scala:516)
//        at akka.actor.ActorCell.invoke(ActorCell.scala:487)
//        at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:254)
//        at akka.dispatch.Mailbox.run(Mailbox.scala:221)
//        at akka.dispatch.Mailbox.exec(Mailbox.scala:231)
//        at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
//        at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.pollAndExecAll(ForkJoinPool.java:1253)
//        at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1346)
//        at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
//        at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)