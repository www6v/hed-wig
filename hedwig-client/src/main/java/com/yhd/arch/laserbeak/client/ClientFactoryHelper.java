/**
 * 
 */
package com.yhd.arch.laserbeak.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.laserbeak.client.locator.ServiceKeeper;
import com.yhd.arch.photon.core.MethodActor;
import com.yhd.arch.photon.core.MethodActorManager;
import com.yhd.arch.photon.core.RemoteMetaData;
import com.yhd.arch.photon.core.ServiceInfo;
import com.yhd.arch.photon.emitter.router.BalancerType;
import com.yhd.arch.photon.util.ActorNameUtil;
import com.yhd.arch.photon.util.ClientSystem;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;

import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * @author root
 *
 */
public class ClientFactoryHelper {

	private static Logger logger = LoggerFactory.getLogger(ClientFactoryHelper.class);

	public static RemoteMetaData createMeta(ClientProfile profile) {
		String serviceName = profile.getServiceName();
		String callModel = profile.getRequestType();
		long timeout = profile.getTimeout();
		long readTimeout = profile.getReadTimeout();
		Class objType = profile.getServiceInterface();
		boolean isRetry = profile.isRedoAble();
		int senderCount = profile.getSenderCount();
		BalancerType rt = BalancerType.getByName(profile.getBalanceAlgo());
		RemoteMetaData meta = new RemoteMetaData(serviceName, callModel, timeout, readTimeout, objType, isRetry, senderCount, rt);
		StringBuilder sb = new StringBuilder(profile.getParentPath());
		meta.setLookupPath(sb.toString());
		if (isRetry) {
			meta.setNonRetryMethods(profile.getNoRetryMethods());
		}
		String user = profile.getUser();
		String passwd = profile.getPassword();
		meta.setTarget(profile.getTarget());
		if (!HedwigUtil.isBlankString(user) && !HedwigUtil.isBlankString(passwd)) {
			meta.setAuthorization(HedwigUtil.genAuthorization(user, passwd));
		}
		meta.setProfileUUId(profile.getProfileUUId());
		return meta;
	}

	public static ServiceKeeper createServiceLookuper(ClientProfile profile, ServiceInfo info) throws HedwigException {
		List<String> l = info.getMethodList();

		for (String mName : l) {
			String actorPath = ActorNameUtil.generateUniqName(info.getMeta().getServiceName(), mName, info.getMeta().getProfileUUId());
			ActorRef methodActor = ClientSystem.getInstance().getActorSystem().actorOf(Props.create(MethodActor.class, info, mName),
					MethodActor.class.getSimpleName() + "_" + actorPath);
			MethodActorManager.getInStance().addActor(actorPath, methodActor);
			logger.info(InternalConstants.LOG_PROFIX + mName + " has initialized," + methodActor);
		}
		ServiceKeeper lookupHelper = new ServiceKeeper(profile, info);
		return lookupHelper;
	}
}
