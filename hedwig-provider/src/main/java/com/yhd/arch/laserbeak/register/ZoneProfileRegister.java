/**
 *
 */
package com.yhd.arch.laserbeak.register;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yhd.arch.zone.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.laserbeak.common.util.RoutePriorityUtil;
import com.yhd.arch.zone.RoutePriority;
import com.yhd.arch.zone.ZkClusterUsage;
import com.yhd.arch.zone.ZoneContainer;
import com.yhd.arch.zone.bean.MethodCrossZoneInfo;
import com.yhd.arch.zone.bean.ServiceCrossZoneInfo;
import com.yhd.arch.zone.util.ZonePathUtil;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.register.IServiceProviderRegister;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.ZkClient;

/**
 * @author root
 *
 */
public class ZoneProfileRegister implements IServiceProviderRegister<ServiceProfile> {

	private static Logger logger = LoggerFactory.getLogger(ZoneProfileRegister.class);
	private Map<String, ServiceProfile> zoneProfiles = new HashMap<String, ServiceProfile>();
	private Map<String, ZoneProfileKeeper> profileKeepers = new HashMap<String, ZoneProfileKeeper>();
	private String czInfoPath;
	private ServiceProfile originProfile;
	private volatile boolean isRegisted = false;
	private ZkClient localZk;
	private IZkChildListener poolCrossListener;
	private IZkDataListener serviceCrossListener;
	private Lock lock = new ReentrantLock();

	public ZoneProfileRegister() {
		super();
		localZk = ZoneContainer.getInstance().getLocalZkClient(ZkClusterUsage.SOA);
	}

	@Override
	public void regist(ServiceProfile profile) throws InvalidParamException {
		originProfile = profile;
		czInfoPath = ZonePathUtil.getCrossZoneServicePath(originProfile);
		try {
			if (localZk.exists(czInfoPath)) {
				ServiceCrossZoneInfo sczi = localZk.readData(czInfoPath, true);
				genZoneProfile(sczi);
				localZk.subscribeDataChanges(czInfoPath, createServiceCrossListener());
			} else {
				genLocalProfile();
			}
			//如果/FlagsCenter/yihaodian#poolXX/crossZone_hedwig路径不存在，则创建，最后添加监听
			String crossZonePoolPath=ZonePathUtil.getCrossZonePoolPath(originProfile);
			if(!localZk.exists(crossZonePoolPath)){
				localZk.createPersistent(crossZonePoolPath,true);
			}
			localZk.subscribeChildChanges(crossZonePoolPath, createPoolCrossListener());
			registAllZoneProfile();
			isRegisted = true;
		} catch (Exception e) {
			logger.error("Regist service failed!!!, serviceProfile:" + originProfile, e);
		}
	}

	private IZkChildListener createPoolCrossListener() {
		if (poolCrossListener == null) {
			poolCrossListener = new IZkChildListener() {

				@Override
				public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
					if (currentChilds!=null&&currentChilds.contains(originProfile.getServiceName())) {
						ServiceCrossZoneInfo sczi = localZk.readData(czInfoPath, true);
                         //如果读取不到，睡100ms再尝试读，最多尝试5次
                        if(sczi==null) {
                            for (int i = 0; i < 5; i++) {
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                sczi = localZk.readData(czInfoPath, true);
                                if (sczi != null) {
                                    break;
                                }
                            }
                        }
						genZoneProfile(sczi);
						registAllZoneProfile();
						localZk.subscribeDataChanges(czInfoPath, createServiceCrossListener());
						if(logger.isDebugEnabled()){
							logger.debug("####createPoolCrossListener.handleChildChange() done!! parentPath="+parentPath+",sczi="+sczi+"####");
						}
					}
				}
			};
		}
		return poolCrossListener;
	}

	private IZkDataListener createServiceCrossListener() {
		if (serviceCrossListener == null) {
			serviceCrossListener = new IZkDataListener() {

				@Override
				public void handleDataDeleted(String dataPath) throws Exception {
					String localZone = ZoneContainer.getInstance().getLocalZoneName();
					Iterator<Map.Entry<String, ZoneProfileKeeper>> it = profileKeepers.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, ZoneProfileKeeper> entry = it.next();
						if (!localZone.equals(entry.getKey())) {
							ZoneProfileKeeper keeper = entry.getValue();
							if (keeper != null) {
								keeper.unRegist();
							}
							zoneProfiles.remove(entry.getKey());
							it.remove();
						}
					}
					if(logger.isDebugEnabled()){
						logger.debug("####createServiceCrossListener.handleDataDeleted() done!! dataPath="+dataPath+"####");
					}
				}

				@Override
				public void handleDataChange(String dataPath, Object data) throws Exception {
					ServiceCrossZoneInfo sczi = localZk.readData(czInfoPath, true);
					genZoneProfile(sczi);
					registAllZoneProfile();
					if(logger.isDebugEnabled()){
						logger.debug("####createServiceCrossListener.handleDataChange() done!! dataPath="+dataPath+"####");
					}
				}
			};
		}
		return serviceCrossListener;
	}

	private void registAllZoneProfile() {
		for (ServiceProfile sp : zoneProfiles.values()) {
			registZoneProfile(sp);
		}
	}

	private void registZoneProfile(ServiceProfile sp) {
		String zone = sp.getRegZone();
		ZkClient zkCli = ZoneContainer.getInstance().getZkClient(zone, ZkClusterUsage.SOA);
		if (zkCli != null) {
			ZoneProfileKeeper zpk = profileKeepers.get(zone);
			if (zpk == null) {
				try {
					zpk = new ZoneProfileKeeper(zkCli, sp);
					profileKeepers.put(zone, zpk);
					zpk.regist();
				} catch (Exception e) {
					logger.error("Create instance of ZoneProfileKeeper for Zone:" + zone + " failed!!!");
				}
			} else {
				zpk.updateProfile(sp);
			}
		} else {
			logger.error("Can't find zk cluster for zone:" + zone);
		}

	}

	private void genLocalProfile() {
		String lz = ZoneContainer.getInstance().getLocalZoneName();
		String lidc=ZoneContainer.getInstance().getIdcContainer().getLocalIdc();
		String lLevel= ZoneContainer.getInstance().getLevel().getCode();
		ServiceProfile zp;
		try {
			zp = originProfile.clone();
			zp.setPubZone(lz);
			zp.setRegZone(lz);

			zp.setProviderLevel(lLevel);
			zp.setPubIdc(lidc);
			zp.setRegIdc(lidc);

			Map<String, RoutePriority> methodRP = createDefaultMethodRP(zp);
			zp.setMethodRP(methodRP);
			zoneProfiles.put(lz, zp);
		} catch (CloneNotSupportedException e) {
			logger.error("ServiceProfile clone failed!!!", e);
		}
	}

	private Map<String, RoutePriority> createDefaultMethodRP(ServiceProfile profile) {
		Map<String, RoutePriority> methodRP = new HashMap<String, RoutePriority>();
		if (profile != null) {
			methodRP = RoutePriorityUtil.createDefaultMethodRP(profile.getMehodNames());
		}
		return methodRP;
	}

	private void genZoneProfile(ServiceCrossZoneInfo sczi) {
		if (sczi != null) {
			lock.lock();
			try {
				Map<String, List<MethodCrossZoneInfo>> mMap = sczi.getMethod4Zone();
				if (mMap != null && mMap.size() > 0) {
					Map<String, Map<String, RoutePriority>> zMzp = new HashMap<String, Map<String, RoutePriority>>();
					for (List<MethodCrossZoneInfo> list : mMap.values()) {
						for (MethodCrossZoneInfo mczi : list) {
							String zn = mczi.getZoneName();
							Map<String, RoutePriority> rMap = zMzp.get(zn);
							if (rMap == null) {
								rMap = createDefaultMethodRP(originProfile);
								zMzp.put(zn, rMap);
							}
							// 跨zone服务配置覆盖默认配置
							rMap.put(mczi.getMethodName(), mczi.getRouterPriority());
						}
					}
					if (zMzp != null && zMzp.size() > 0) {
						Map<String, ServiceProfile> newZoneProfiles = new HashMap<String, ServiceProfile>();
						for (String zone : zMzp.keySet()) {
							try {
								Map<String, RoutePriority> mrMap = zMzp.get(zone);
								if (mrMap != null && mrMap.size() > 0) {
									ServiceProfile sp = zoneProfiles.remove(zone);
									if (sp == null) {
										sp = originProfile.clone();
									}
									sp.setMethodRP(mrMap);
									sp.setRegZone(zone);
									sp.setPubZone(ZoneContainer.getInstance().getLocalZoneName());

									newZoneProfiles.put(zone, sp);
								}

							} catch (CloneNotSupportedException e) {
								logger.error("ServiceProfile clone failed!!!", e);
							}
						}
						unRegistZoneProfiles(zoneProfiles);
						zoneProfiles = newZoneProfiles;
						// 如果所有跨zone的配置不包括本地zone,则默认生成本地zone默认配置
						String localZone = ZoneContainer.getInstance().getLocalZoneName();
						if (zMzp.keySet().contains(localZone) == false) {
							genLocalProfile();
						}
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}

	private void unRegistZoneProfiles(Map<String, ServiceProfile> zoneProfilesMap) {
		if (zoneProfilesMap.size() > 0) {
			for (String zone : zoneProfilesMap.keySet()) {
				ZoneProfileKeeper zpk = profileKeepers.remove(zone);
				if (zpk != null) {
					zpk.unRegist();
				}
			}
		}
	}

	@Override
	public void updateProfile(ServiceProfile newProfile) {
		while (!isRegisted) {
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
		if (!originProfile.equals(newProfile)) {
			originProfile = newProfile;
			for (String zone : zoneProfiles.keySet()) {
				try {
					ServiceProfile sp = zoneProfiles.get(zone);
					ServiceProfile nsp = originProfile.clone();
					nsp.setPubZone(sp.getPubZone());
					nsp.setRegZone(sp.getRegZone());

					nsp.setProviderLevel(sp.getProviderLevel());
					nsp.setPubIdc(sp.getPubIdc());
					nsp.setRegIdc(sp.getRegIdc());

					nsp.setMehodNames(sp.getMehodNames());
					nsp.setMethodRP(sp.getMethodRP());
					zoneProfiles.put(zone, nsp);
					ZoneProfileKeeper zpk = profileKeepers.get(zone);
					if (zpk != null) {
						zpk.updateProfile(nsp);
					}
				} catch (CloneNotSupportedException e) {
					logger.error("ServiceProfile clone failed!!!", e);
				}
			}
		}
	}

	@Override
	public void unRegist(ServiceProfile profile) {
		String zone = profile.getRegZone();
		if (!HedwigUtil.isBlankString(zone)) {
			ZoneProfileKeeper zpk = profileKeepers.get(zone);
			if (zpk != null) {
				zpk.unRegist();
			}
		} else {
			for (ZoneProfileKeeper zpk : profileKeepers.values()) {
				zpk.unRegist();
			}
		}

	}

}
