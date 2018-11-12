/**
 *
 */
package com.yhd.arch.laserbeak.client.locator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.photon.common.HostInfo;
import com.yhd.arch.photon.common.ProfileUtil;
import com.yhd.arch.photon.constants.ProtocolType;
import com.yhd.arch.photon.core.ServiceInfo;
import com.yhd.arch.photon.emitter.event.change.HostInfoChangeEvent;
import com.yhd.arch.photon.emitter.event.change.ServiceHostChangeEvent;
import com.yhd.arch.photon.emitter.event.init.RouterBuildEvent;
import com.yhd.arch.photon.util.ClientSystem;
import com.yihaodian.architecture.hedwig.balancer.special.GrayInfo;
import com.yihaodian.architecture.hedwig.client.event.engine.HedwigEventEngine;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;

import akka.actor.ActorRef;

/**
 * @author root
 */
public class ServiceNodeLocator {
	private static Logger logger = LoggerFactory.getLogger(ServiceNodeLocator.class);
	protected ZkClient _zkClient = null;
	protected Map<String, List<String>> campMap = new HashMap<String, List<String>>();
	protected Map<String, ServiceProfile> profileContainer = new ConcurrentHashMap<String, ServiceProfile>();
	protected Set<String> whiteList = new HashSet<String>();
	protected static boolean isProfileSensitive = false;
	protected boolean initialized = false;
	protected boolean grayInitialized = false;
	protected ClientProfile clientProfile;
	protected ScheduledExecutorService ses = HedwigEventEngine.getEngine().getStpes();
	protected int DELY = InternalConstants.DEFAULT_SCHEDULER_DELY;
	protected ServiceInfo info;
	protected String parentPath;
	protected String providerPoolName;
	protected String grayPath;
	protected String flagPath;
	protected Set<String> campSet;
	protected String baseCamp;

	public ServiceNodeLocator(ClientProfile profile, ServiceInfo info) throws HedwigException {
		super();
		this.clientProfile = profile;
		this.info = info;
		this.DELY = ProperitesContainer.client().getIntProperty(PropKeyConstants.HEDWIG_SCHEDULER_POOL_DELY, this.DELY);
		String target = clientProfile.getTarget();
		if (HedwigUtil.isBlankString(target)) {
			this.isProfileSensitive = clientProfile.isProfileSensitive();
			this._zkClient = ZkUtil.getZkClientInstance();
			loadServiceProfile();
			loadMemberWhiteList();
			loadGray();
		} else {
			whiteList = null;
			genTargetServiceProfile(target);
		}
		notifyQualifiedMembers();
	}

	/*************************************************
	 * request specify node
	 **********************************************************************************************/
	private void genTargetServiceProfile(String target) {
		if (target.contains(",")) {
			String[] targets = target.split(",");
			for (String strUrl : targets) {
				addTargetSp2Container(strUrl);
			}
		} else {
			addTargetSp2Container(target);
		}

	}

	private void addTargetSp2Container(String strUrl) {
		ServiceProfile sp = UrlHelper.genServiceProfileFromStringUrl(strUrl, clientProfile.getServiceName(),
				clientProfile.getServiceInterface());
		if (sp != null) {
			addServiceProfile2Container(sp.getHostString(), sp, profileContainer);
		}
	}

	/*************************************************
	 * for gray publish
	 **********************************************************************************************/
	private void loadGray() {
		List<String> dict = _zkClient.getChildren(InternalConstants.HEDWIG_PAHT_APPDICT);
		providerPoolName = HedwigClientUtil.getServPoolName(clientProfile, dict);
		if (!HedwigUtil.isBlankString(providerPoolName)) {
			loadGrayInfo(providerPoolName);
			this.grayInitialized = true;
		} else {
			logger.error("Pool:" + providerPoolName + " not support gray,u can fix this problem by upgrade hedwig-provider above 0.1.3");
		}
	}

	protected void loadGrayInfo(String poolName) {
		flagPath = HedwigUtil.genPoolFlagsPath(poolName);
		grayPath = flagPath + InternalConstants.FLAG_GRAY;
		if (_zkClient.exists(grayPath)) {
			byte[] rawData = _zkClient.readRawData(grayPath, true);
			parseGrayNode(rawData);
		}
	}

	protected void parseGrayNode(byte[] rawData) {
		String resultPath = grayPath + InternalConstants.FLAG_GRAY_RESULT;
		if (rawData != null && rawData.length > 0) {
			GrayInfo gi = cast2GrayInfo(rawData);
			if (gi != null) {
				if (gi.getValidateCode() == 0) {
					setGrayInfo2Service(gi);
					ClientSystem.getInstance().getServiceManager().tell(new RouterBuildEvent(this.info), ActorRef.noSender());
				}
				String result = generateResult(gi);
				if (!_zkClient.exists(resultPath)) {
					_zkClient.createPersistent(resultPath);
					_zkClient.writeRawData(resultPath, result);
				} else {
					byte[] barr = _zkClient.readRawData(resultPath, false);
					if (barr != null) {
						String rr = new String(barr);
						if (!rr.contains(gi.getPublishId())) {
							_zkClient.writeRawData(resultPath, result);
						}
					} else {
						_zkClient.writeRawData(resultPath, result);
					}
				}
			} else {
				logger.error("There is no gray data or the data can not be parsed by hedwig!!!");
				_zkClient.writeRawData(resultPath, rawData);
			}
		}
	}

	private GrayInfo cast2GrayInfo(byte[] rawData) {
		GrayInfo info = null;
		if (rawData != null && rawData.length > 0) {
			String str = new String(rawData);
			if (str.contains(";")) {
				info = new GrayInfo();
				String[] sArr = str.split(";");
				if (sArr != null && sArr.length > 1) {
					info.setPublishId(sArr[0]);
					info.setRange(sArr[1]);
					info.setGraySet(sArr[2]);
				}
			}
		}
		return info;
	}

	private String generateResult(GrayInfo gi) {
		String pid = gi.getPublishId();
		StringBuilder sb = new StringBuilder(pid);
		int code = gi.getValidateCode();
		sb.append(";").append(code);
		if (code != 0) {
			sb.append(";").append(gi.toString());
		}
		sb.append(";");
		return sb.toString();
	}

	protected void setGrayInfo2Service(GrayInfo gi) {
		if (gi != null) {
			HashSet<String> grayHosts = gi.getGraySet() != null ? (HashSet<String>) gi.getGraySet() : null;
			this.info.setGrayHost(grayHosts);
			this.info.setGrayOffset(gi.getStart());
		}
	}

	/*************************************************
	 * load node white list
	 **********************************************************************************************/
	protected void loadMemberWhiteList() {
		baseCamp = ZkUtil.createBaseCampPath(clientProfile);
		List<String> camps = _zkClient.getChildren(baseCamp);
		campMap = new HashMap<String, List<String>>();
		if (camps != null && camps.size() > 0) {
			//如果只有1个分组
			if (camps.size() == 1) {
				String campName = camps.get(0);
				//并且campName=refugee 则将所有可用机器IP放进campMap
				if (InternalConstants.HEDWIG_PAHT_REFUGEE.equals(campName)) {
					List<String> list = new ArrayList<String>();
					list.addAll(profileContainer.keySet());
					try {
						String campPath = ZkUtil.createCampPath(clientProfile, campName);
						campMap.put(campPath, list);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				for (String campName : camps) {//加载所有分组ip列表到campMap
					loadCampMember(campName);
				}
			}
		}
		whiteList = null;
	}

	public void loadCampMember(String campName) {
		try {
			if (!InternalConstants.HEDWIG_PAHT_REFUGEE.equals(campName)) {
				fillRefugeeGroup();
			}
			String campPath = ZkUtil.createCampPath(clientProfile, campName);
			List<String> list = _zkClient.getChildren(campPath);
			campMap.put(campPath, list);
		} catch (Exception e) {
			logger.error(InternalConstants.HANDLE_LOG_PROFIX + e.getMessage(), e);
		}

	}

	/**
	 * Fill the refugee camp with all candidate when first customized camp
	 * create.
	 */
	private void fillRefugeeGroup() {
		try {
			String refugeePath = ZkUtil.createCampPath(clientProfile, InternalConstants.HEDWIG_PAHT_REFUGEE);
			List<String> list = _zkClient.getChildren(refugeePath);
			if (list == null || list.size() == 0) {
				for (String host : profileContainer.keySet()) {
					String fpath = refugeePath + "/" + host;
					if (!_zkClient.exists(fpath)) {
						_zkClient.createPersistent(fpath);
					}
				}
			}
		} catch (Exception e) {
			logger.error(InternalConstants.HANDLE_LOG_PROFIX + e.getMessage(), e);
		}

	}

	private Set<String> genWhitList() {
		Set<String> set = new HashSet<String>();
		for (List<String> pl : campMap.values()) {
			set.addAll(pl);
		}
		return set;
	}

	protected void updateCampMembers(String campPath, List<String> currentChilds) {
		if (campMap.size() == 1 && campPath.contains(InternalConstants.HEDWIG_PAHT_REFUGEE) && currentChilds.size() == 0) {
			List<String> list = new ArrayList<String>();
			list.addAll(profileContainer.keySet());
			campMap.put(campPath, list);
		} else {
			campMap.put(campPath, currentChilds);
		}

		//		whiteList = genWhitList();
		notifyQualifiedMembers();
	}

	/*************************************************
	 * load node profile
	 **********************************************************************************************/
	private void loadServiceProfile() {
		parentPath = clientProfile.getParentPath();
		List<String> childList = null;
		if (parentPath != null) {
			if (!_zkClient.exists(parentPath)) {
				logger.error("Can't find path " + parentPath + " in ZK. Can't find service provider for now");
				_zkClient.createPersistent(parentPath, true);
			}
			childList = _zkClient.getChildren(parentPath);
			loadChildData(parentPath, childList);
		}
		initialized = true;
	}

	private void loadChildData(final String parentPath, List<String> childList) {
		if (childList != null && childList.size() > 0) {
			for (String child : childList) {
				String childPath = HedwigUtil.getChildFullPath(parentPath, child);
				if (_zkClient.exists(childPath)) {
					Object obj = _zkClient.readData(childPath, true);
					addServiceProfile2Container(child, obj, profileContainer);
				}
			}
		}
	}

	protected void addServiceProfile2Container(String child, Object obj, Map<String, ServiceProfile> container) {
		if (obj != null) {
			ServiceProfile sp = (ServiceProfile) obj;
			if (ProtocolType.HTTP.equals(ProtocolType.getByPrefix(sp.getProtocolPrefix()))) {
				logger.error(
						"Hedwig does not support mixed mode currently. For example,  akka client which has been mapped to hessian server is not allowed, and the like. Since this kind of scenario could lead to terrible performance issue in some special situations, so the system will exit exceptionally. More information for trouble shooting[serviceName="
								+ sp.getServiceName() + " protocolType=" + sp.getProtocolPrefix() + "]");
				System.exit(-1);
				//TransferInfoMapping.putTransferInfo(sp.getHostString(), sp.getServiceName(), sp.getServiceUrl(), sp.getCodecName());
			}
			container.put(child, sp);
		}

	}

	protected void notifyQualifiedMembers() {
		Collection<ServiceProfile> c = profileContainer.values();//BalancerUtil.filte(profileContainer.values(), whiteList);
		List<HostInfo> l = new ArrayList<HostInfo>();
		Map<String, String> map = new HashMap<String, String>();
		for (ServiceProfile s : c) {
			HostInfo hi = ProfileUtil.generateHostInfo(s);
			l.add(hi);
			map.put(hi.getHostUrl(), s.getCodecName());
		}
		this.info.setHostCodeTypeMap(map);
		Map<String, List<String>> hostGroupNamesMap = buildHostToGroupMap(campMap);
		this.info.setHostGroupNamesMap(hostGroupNamesMap);
		ServiceHostChangeEvent shce = new ServiceHostChangeEvent(this.info, l);
		logger.info(InternalConstants.LOG_PROFIX + this.info.getMeta().getServiceName() + " remote nodes update!!!");
		ClientSystem.getInstance().getServiceManager().tell(shce, ActorRef.noSender());
	}

	private Map<String, List<String>> buildHostToGroupMap(Map<String, List<String>> groupToHostMap) {
		Map<String, List<String>> resultMap = new ConcurrentHashMap<String, List<String>>();
		if (groupToHostMap != null && groupToHostMap.size() > 0) {
			for (Map.Entry<String, List<String>> entry : groupToHostMap.entrySet()) {
				String key = entry.getKey();
				int index = key.indexOf(InternalConstants.HEDWIG_PAHT_CAMPS) + InternalConstants.HEDWIG_PAHT_CAMPS.length() + 1;
				String groupName = key.substring(index);
				List<String> hostList = entry.getValue();
				for (String host : hostList) {
					if (resultMap.get(host) == null) {
						List<String> groupList = new ArrayList<String>();
						groupList.add(groupName);
						resultMap.put(host, groupList);
					} else {
						resultMap.get(host).add(groupName);
					}
				}
			}
		}
		return resultMap;
	}

	public void updatProfile(String child, Object obj) {
		if (obj != null) {
			ServiceProfile nsp = (ServiceProfile) obj;
			ServiceProfile osp = profileContainer.get(child);
			if (osp != null) {
				notifyHostInfoChange(nsp);
				osp.update(nsp);
			}
		}
	}

	protected void notifyHostInfoChange(ServiceProfile nsp) {
		HostInfoChangeEvent nwce = new HostInfoChangeEvent(info, ProfileUtil.generateHostInfo(nsp));
		ClientSystem.getInstance().getServiceManager().tell(nwce, ActorRef.noSender());
	}
}
