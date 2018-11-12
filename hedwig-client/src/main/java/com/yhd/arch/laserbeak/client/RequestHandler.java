/**
 *
 */
package com.yhd.arch.laserbeak.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.laserbeak.client.meta.HedwigClientConfigUtil;
import com.yhd.arch.laserbeak.client.util.MonitorLogUtil;
import com.yhd.arch.photon.common.parallel.ParallelRequest;
import com.yhd.arch.photon.common.parallel.ParallelToolkit;
import com.yhd.arch.photon.constants.Constants;
import com.yhd.arch.photon.core.RemoteMetaData;
import com.yhd.arch.photon.core.RemoteResponse;
import com.yhd.arch.photon.core.ServiceInfo;
import com.yhd.arch.photon.emitter.router.RouteeWraper;
import com.yhd.arch.photon.exception.PhotonException;
import com.yhd.arch.photon.invoker.ActorInvoker;
import com.yhd.arch.photon.invoker.DefaultRequest;
import com.yhd.arch.photon.util.ActorNameUtil;
import com.yihaodian.architecture.hedwig.client.util.HedwigMonitorClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ArgsMeta;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigGlobalIdVo;
import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.common.util.InvocationContext;
import com.yihaodian.common.KeyUtil;
import com.yihaodian.monitor.dto.ClientBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import com.yihaodian.monitor.util.MonitorJmsSendUtil;

import akka.dispatch.Futures;
import org.springframework.aop.support.AopUtils;
import scala.concurrent.Future;
import scala.concurrent.Promise;

/**
 * @author root
 */
public class RequestHandler implements InvocationHandler {

	private static Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	private ServiceInfo info;
	private RemoteMetaData meta;
	private ClientProfile profile;
	private IRequestListener listener = new DefaultRequestListener();

	public RequestHandler() {

	}

	public RequestHandler(ClientProfile profile, RemoteMetaData meta, ServiceInfo info) throws HedwigException {
		this.profile = profile;
		this.meta = meta;
		this.info = info;
	}

	public RequestHandler(ClientProfile profile, RemoteMetaData meta, ServiceInfo info, IRequestListener listener) throws HedwigException {
		this.profile = profile;
		this.meta = meta;
		this.info = info;
		if (listener != null) {
			this.listener = listener;
		}

	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		listener.before(proxy, method, args);
		String mn = method.getName();
		Object rtnObj = null;
		if (ActorNameUtil.isIgnore(mn)) {
			if (AopUtils.isHashCodeMethod(method)) {
				return this.hashCode();
			} else if (AopUtils.isToStringMethod(method)) {
				return this.toString();
			} else {
				throw new HedwigException(mn + " is not supported by proxy");
			}
		}
		HedwigGlobalIdVo globalIdVo = HedwigContextUtil.getGlobalIdVo();
		InvocationContext context = HedwigContextUtil.getInvocationContext().clone();
		context.removeValue(InternalConstants.HEDWIG_METHOD_ARGUMENTS_KEY);// 清除context参数值，新版使用新方法传递
		DefaultRequest<InvocationContext> request = new DefaultRequest<InvocationContext>(this.info, method, args, context);
		// 根据方法级别的远程配置实时拦截更新本地超时配置
		request = HedwigClientConfigUtil.updateDefaultRequest(request, profile);
		if (request != null && request.isInvokeEnabled() == false) {
			throw new HedwigException("###The Request Forbidden!!clientPoolId=" + profile.getClientPoolId() + ",providerPoolId="
					+ profile.getProviderPoolId());
		}
		String reqId = KeyUtil.getReqId(profile.getServiceAppName(), new Date(request.getCreateMillisTime()));
		request.getContext().putValue(InternalConstants.HEDWIG_REQUEST_ID, reqId);
		request.createMillisTime();
		ClientBizLog cbLog = MonitorLogUtil.createBizLog(request, profile);
		String callType = ParallelRequest.getLocalCallType();
		if (callType == null) {
			callType = HedwigContextUtil.getString(PropKeyConstants.PHOTON_CALL_MODEL, this.meta.getCallMode());
		}
		if (Constants.CALL_SYNC.equalsIgnoreCase(callType)) {
			// 同步返回
			RemoteResponse response = null;
			try {
				response = ActorInvoker.getInstance().invokeSync(request);
				if (response != null) {
					rtnObj = response.getReturn();
				}
				cbLog.setRespTime(new Date());
				cbLog.setSuccessed(MonitorConstants.SUCCESS);
			} catch (Exception e) {
				HedwigException he = wrapException(e);
				ArgsMeta metas = HedwigMonitorUtil.generateMeta(request.getParameters());
				cbLog.setInParamObjects(metas);

				HedwigMonitorClientUtil.setException(cbLog, he);

				/*
				 * Updated by Frank wang on Dec 26th, 2016.
				 *
				 * Returning the target exception instead of other wrapped exception, such as InvocationTargetException or UndeclaredThrowableException,
				 * which will fix the bugs that some functions depend on exception hierarchy.
				 */
				throw new RuntimeException(he);
			} finally {
				RouteeWraper routee = request.getLastRoutee();
				String hostStr = routee != null ? routee.getHostUrl() : "UnKonwHost";
				cbLog.setProviderHost(hostStr);

				if (routee != null) {
					cbLog.setProviderLevel(routee.getLevel());
					cbLog.setProviderIDC(routee.getIdc());
					cbLog.setProviderZone(routee.getZone());
				}
				cbLog.setMemo(cbLog.getMemo() + ",hedwigVersion:laserbeak");
				cbLog.setLayerType(MonitorConstants.LAYER_TYPE_ENGINE);
				cbLog.setCurtLayer(request.getContext().getHopValue());
				cbLog.setLocalLayer(System.nanoTime());
				cbLog.setCommId(request.getSequence() + "");
				cbLog.setParentId(HedwigContextUtil.getInvocationContext().getStrValue(InternalConstants.HEDWIG_REQUEST_PARENT_ID, ""));
				MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
				HedwigContextUtil.cleanGlobal(globalIdVo);
				listener.after(proxy, method, args);
			}
			return rtnObj;
		} else if (Constants.CALL_FUTURE.equals(callType)) {
			// future set
			Promise promise = ParallelRequest.getLocalPromise();
			if (promise == null) {
				promise = Futures.promise();
			}
			Future<RemoteResponse> future = promise.future();
			try {
				ActorInvoker.getInstance().invokeFuture(request, promise);
				ParallelToolkit.setFuture(request.getMethodName(), future);
				cbLog.setRespTime(new Date());
				cbLog.setSuccessed(MonitorConstants.SUCCESS);
			} catch (Exception e) {
				HedwigException he = wrapException(e);
				ArgsMeta metas = HedwigMonitorUtil.generateMeta(request.getParameters());
				cbLog.setInParamObjects(metas);
				HedwigMonitorClientUtil.setException(cbLog, he);
				throw he;
			} finally {
				RouteeWraper routee = request.getLastRoutee();
				String hostStr = routee != null ? routee.getHostUrl() : "UnKonwHost";
				cbLog.setProviderHost(hostStr);
				cbLog.setMemo(cbLog.getMemo() + ",hedwigVersion:laserbeak");
				cbLog.setLayerType(MonitorConstants.LAYER_TYPE_ENGINE);
				cbLog.setCurtLayer(request.getContext().getHopValue());
				cbLog.setLocalLayer(System.nanoTime());
				cbLog.setCommId(request.getSequence() + "");
				MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
				ParallelRequest.clearLocalPromise();
				HedwigContextUtil.cleanGlobal(globalIdVo);
				listener.after(proxy, method, args);
			}

			return getVirReturnForOneWay(method.getReturnType());
		} else if (Constants.CALL_ONEWAY.equals(callType)) {
			// 执行不返回
			ActorInvoker.getInstance().invokeOneWay(request);
			HedwigContextUtil.cleanGlobal(globalIdVo);
			listener.after(proxy, method, args);
			return getVirReturnForOneWay(method.getReturnType());
		}
		listener.after(proxy, method, args);
		throw new PhotonException("callmethod configure is error:" + callType);

	}

	@SuppressWarnings("rawtypes")
	private Object getVirReturnForOneWay(Class returnType) {
		if (returnType == byte.class) {
			return (byte) 0;
		} else if (returnType == short.class) {
			return (short) 0;
		} else if (returnType == int.class) {
			return 0;
		} else if (returnType == boolean.class) {
			return false;
		} else if (returnType == long.class) {
			return 0l;
		} else if (returnType == float.class) {
			return 0.0f;
		} else if (returnType == double.class) {
			return 0.0d;
		} else {
			return null;
		}
	}

	private HedwigException wrapException(Exception e) {
		HedwigException he = null;
		if (e != null) {
			String errMsg = profile.toString();
			he = new HedwigException(errMsg, e);
		}
		return he;
	}
}
