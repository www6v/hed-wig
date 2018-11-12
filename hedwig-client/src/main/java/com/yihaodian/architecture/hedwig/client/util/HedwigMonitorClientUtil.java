/**
 * 
 */
package com.yihaodian.architecture.hedwig.client.util;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.yhd.arch.container.RootContainer;
import com.yhd.arch.zone.ZoneContainer;
import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.monitor.dto.ClientBizLog;
import com.yihaodian.monitor.util.MonitorConstants;

/**
 * @author root
 * 
 */
public class HedwigMonitorClientUtil {

	public static ClientBizLog createClientBizLog(BaseEvent event, HedwigContext context, String reqId, String globalId, Date reqTime) {
		BaseEvent be = event;
		ClientProfile profile = context.getClientProfile();
		ClientBizLog cbLog = new ClientBizLog();
		try {
			cbLog.setServicePath(ZkUtil.createAppPath(context.getClientProfile()));
		} catch (InvalidParamException e) {
			e.printStackTrace();
		}
		getCallZone(cbLog);
		String callApp = RootContainer.getInstance().getAppName();
		callApp = HedwigUtil.isBlankString(callApp)?profile.getClientAppName():callApp;
		cbLog.setCallApp(callApp);
		cbLog.setCallHost(ProperitesContainer.client().getProperty(PropKeyConstants.HOST_IP));
		cbLog.setUniqReqId(globalId);
		cbLog.setServiceName(profile.getServiceName());
		List<String> dict = HedwigClientUtil.getAppPathDict();
		String poolId =HedwigClientUtil.getServPoolName(profile, dict);
		if(HedwigUtil.isBlankString(poolId)){
			poolId = profile.getServiceAppName();
		}
		if(poolId.equals("defaultLaserAppName")){
			poolId = "defaultAppName";
		}
		cbLog.setProviderApp(poolId);
		cbLog.setReqId(reqId);
		cbLog.setServiceVersion(profile.getServiceVersion());
		Set<String> groupSet = profile.getGroupNames();
		String groups = (groupSet == null || groupSet.size() == 0) ? InternalConstants.NON_GROUP : groupSet.toString();
		cbLog.setServiceGroup(groups);
		cbLog.setReqTime(reqTime);
		cbLog.setMethodName(be.getCallerMethod());
		cbLog.setServiceMethodName(be.getServiceMethod());
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
	public static void setException(ClientBizLog cbLog, Throwable exception) {
		cbLog.setRespTime(new Date());
		cbLog.setSuccessed(MonitorConstants.FAIL);
		cbLog.setExceptionClassname(HedwigMonitorUtil.getExceptionClassName(exception));
		cbLog.setExceptionDesc(HedwigMonitorUtil.getExceptionMsg(exception));
	}

}
