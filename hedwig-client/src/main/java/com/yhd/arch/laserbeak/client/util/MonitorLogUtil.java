/**
 * 
 */
package com.yhd.arch.laserbeak.client.util;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.yhd.arch.container.RootContainer;
import com.yhd.arch.photon.core.RemoteRequest;
import com.yhd.arch.zone.ZoneContainer;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.InvocationContext;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.monitor.dto.ClientBizLog;

/**
 * @author root
 *
 */
public class MonitorLogUtil {

	public static ClientBizLog createBizLog(RemoteRequest<InvocationContext> request, ClientProfile profile) {
		ClientBizLog cbLog = new ClientBizLog();
		try {
			cbLog.setServicePath(ZkUtil.createAppPath(profile));
		} catch (InvalidParamException e) {
			e.printStackTrace();
		}
		getCallZone(cbLog);
		cbLog.setCallApp(profile.getClientAppName());
		cbLog.setCallHost(RootContainer.getInstance().getHostIp());
		cbLog.setUniqReqId(request.getGlobalId());
		cbLog.setServiceName(profile.getServiceName());
		List<String> dict = HedwigClientUtil.getAppPathDict();
		String poolId =HedwigClientUtil.getServPoolName(profile, dict);
		if(HedwigUtil.isBlankString(poolId)){
			poolId = profile.getServiceAppName();
		}
		cbLog.setProviderApp(poolId);
		cbLog.setReqId(request.getReqId());
		cbLog.setServiceVersion(profile.getServiceVersion());
		List<String> groupSet = request.getGroupNames();
		String groups = (groupSet == null || groupSet.size() == 0) ? InternalConstants.NON_GROUP : groupSet.toString();
		cbLog.setServiceGroup(groups);
		cbLog.setReqTime(new Date(request.getCreateMillisTime()));
		cbLog.setMethodName(request.getMangleName());
		cbLog.setServiceMethodName(request.getMethodName());
		cbLog.setMemo("Laserbeak");
		return cbLog;
	}

	public static void getCallZone(ClientBizLog cbLog){
		String callZone= ZoneContainer.getInstance().getLocalZoneName();
		String callIDC=ZoneContainer.getInstance().getIdcContainer().getLocalIdc();
		String callLevel=ZoneContainer.getInstance().getLevel().getCode();
		cbLog.setCallZone(callZone);
		cbLog.setCallIDC(callIDC);
		cbLog.setCallLevel(callLevel);
	}
}
