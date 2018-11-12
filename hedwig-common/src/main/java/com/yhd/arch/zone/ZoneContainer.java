/**
 *
 */
package com.yhd.arch.zone;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yhd.arch.idc.IDCContainer;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.LogManagerUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.ZkClient;

/**
 * @author root
 *
 */
public class ZoneContainer {

    private static Logger logger = LoggerFactory.getLogger(ZoneContainer.class);
    private static Map<String, Zone> zones = new HashMap<String, Zone>();
    private static Map<String, ZkClient> zkClients;
    private static Map<String, Double> distances;
    private static Map<String, Long> bandwidthMap;
    private String localZone = "UnknownZone";
    private Lock lock = new ReentrantLock();
    private IDCContainer idcContainer;
    private DeployLevel level = DeployLevel.ZONE;
    private static ZoneContainer container = new ZoneContainer();
    private String zonePath = ZoneConstants.IDC_ZONE_PATH;
    private String localZonePath = "/SOA"+ZoneConstants.ZONE_ROOT;

    public static ZoneContainer getInstance() {

        return container;
    }

    private ZoneContainer() {
        super();
        zkClients = new HashMap<String, ZkClient>();
        distances = new HashMap<String, Double>();
        String tmplevel = ProperitesContainer.provider().getProperty(PropKeyConstants.DEPLOY_LEVEL);
        level = DeployLevel.getByCode(tmplevel);
        initZones();
        Collection<Zone> zoneCollection=null;
        if(zones!=null){
            zoneCollection = zones.values();
        }
        idcContainer = new IDCContainer(zoneCollection);
        watchZoneChange();
        LogManagerUtil.updatePackageLogLevel(ZoneContainer.class.getName(), LogManagerUtil.Level_DEBUG);
        if (logger.isDebugEnabled()) {
            logger.info("\n###---" + ZoneContainer.class.getName() + " init SUCCESS!!---###");
        }
    }

    public IDCContainer getIdcContainer(){
        return idcContainer;
    }

    public ZkClient getLocalZkClient(ZkClusterUsage usage) {
        return getZkClient(localZone, usage.getCode());
    }

    public ZkClient getLocalZkClient(String usageCode) {
        return getZkClient(localZone,usageCode);
    }

    public String getLocalZoneName() {
        return localZone;
    }

    public boolean hasZone(String zoneName) {
        return zones.containsKey(zoneName);
    }

    private void watchZoneChange() {
        try {
            ZkClient localZk = ZkUtil.getZkClientInstance();
            localZk.subscribeChildChanges(zonePath, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    initZones();
                    if (logger.isDebugEnabled()) {
                        logger.debug("####-----ZoneContainer.watchZoneChange()_handleChildChange:initZones=" + getAllZoneName());
                    }
                }
            });
            localZk.subscribeDataChanges(zonePath, new IZkDataListener() {
                @Override public void handleDataChange(String dataPath, Object data) throws Exception {
                    initZones();
                    if (logger.isDebugEnabled()) {
                        logger.debug("####-----ZoneContainer.watchZoneChange()_handleDataChange:initZones=" + getAllZoneName());
                    }
                }

                @Override public void handleDataDeleted(String dataPath) throws Exception {
                    initZones();
                    if (logger.isDebugEnabled()) {
                        logger.debug("####-----ZoneContainer.watchZoneChange()_handleDataDeleted:initZones=" + getAllZoneName());
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    public void initZones() {
        lock.lock();
        try {
            Map<String, Zone> zoneMap = new HashMap<String, Zone>();
            try {
                ZkClient localZk = ZkUtil.getZkClientInstance();
                if(!localZk.exists(zonePath)){
                    if(localZk.exists(ZoneConstants.ZONE_PATH)){
                        zonePath = ZoneConstants.ZONE_PATH;
                        localZonePath = ZoneConstants.ZONE_ROOT;
                    }
                }
                if (!localZk.exists(zonePath)) {
                    localZk.createPersistent(zonePath, true);
                }
                localZone = localZk.readData(localZonePath, true);
                if (HedwigUtil.isBlankString(localZone)) {
                    localZone = ProperitesContainer.provider().getProperty(PropKeyConstants.LOCAL_ZONE_NAME);
                    if (!HedwigUtil.isBlankString(localZone)) {
                        if (localZk.exists(localZonePath)) {
                            localZk.writeData(localZonePath, localZone);
                            Zone z = new Zone();
                            z.setName(localZone);
                            z.setZoneLevel(level);
                            Map<ZkClusterUsage, String> m = new HashMap<ZkClusterUsage, String>();
                            m.put(ZkClusterUsage.SOA, ProperitesContainer.client().getProperty(PropKeyConstants.ZK_SERVER_LIST));
                            z.setZkClusterMap(m);
                            localZk.createPersistent(zonePath + "/" + localZone, z);
                        } else {

                            localZk.createPersistent(localZonePath, localZone);
                        }
                    } else {
                        logger.error("Can't find local zone name in zookeeper-cluster.properties");
                        System.exit(-1);

                    }
                }
                String key = genkey(localZone, ZkClusterUsage.SOA.getCode());
                zkClients.put(key, localZk);
                List<String> zoneList = localZk.getChildren(zonePath);
                if (zoneList != null) {
                    for (String zoneName : zoneList) {
                        String dataPath = zonePath + "/" + zoneName;
                        try {
                            Zone z = localZk.readData(dataPath);
                            if (z != null) {
                                zoneMap.put(z.getName(), z);
                                if(localZone.equals(z.getName()) && z.getZoneLevel()==null){
                                    z.setZoneLevel(level);
                                    localZk.writeData(dataPath,z);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Read zone:" + zoneName + " data error!!!", e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            distances = ZoneCalcHelper.calcDistences(zoneMap);
            bandwidthMap = ZoneCalcHelper.calcBandwidth(zoneMap);
            zones = zoneMap;
        } finally {
            if(idcContainer!=null){
                idcContainer.refresh(zones.values());
            }
            lock.unlock();
        }
    }


    public Double getDistence(String srcZone, String destZone) {
        String key = ZoneCalcHelper.genKey(srcZone, destZone);
        return distances.get(key);
    }

    public Zone getZone(String zone) {
        return zones.get(zone);
    }

    public ZkClient getZkClient(String zone, ZkClusterUsage usage) {
        return getZkClient(zone, usage.getCode());
    }

    public ZkClient getZkClient(String zone, String usageCode) {
        ZkClient cli = null;
        String key = genkey(zone, usageCode);
        if (!zkClients.containsKey(key)) {
            lock.lock();
            try {
                if (!zkClients.containsKey(key)) {
                    Zone z = zones.get(zone);
                    if (z != null) {
                        String clusterString = z.getZkClusterByUsageCode(usageCode);
                        if (!HedwigUtil.isBlankString(clusterString)) {
                            ZkClient client = new ZkClient(clusterString, ZoneConstants.ZK_SESSION_TIMEOUT, Integer.MAX_VALUE);
                            zkClients.put(key, client);
                        } else {
                            throw new RuntimeException("Zone:" + zone + " zk cluster for " + usageCode + " is not exist!!!");
                        }
                    } else {
                        throw new RuntimeException("Zone:" + zone + " is not exist!!!");
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        cli = zkClients.get(key);
        return cli;
    }


    private String genkey(String zone, String usageCode) {
        String key = null;
        if (HedwigUtil.isBlankString(zone)) {
            throw new IllegalArgumentException("Zone name must not null!!!");
        }
        if (usageCode == null) {
            throw new IllegalArgumentException("ZkCluster usage must not null!!!");
        }
        key = zone + "_" + usageCode;
        return key;
    }

    public Set<String> getAllZoneName() {
        Set<String> az = new HashSet<String>();
        if (zones != null) {
            for (String zn : zones.keySet()) {
                az.add(zn);
            }
        }
        return az;
    }

    public Set<Zone> getAllZone() {
        return new HashSet<Zone>(zones.values());
    }

    public Long getBandwidth(String srcZone, String destZone) {
        String key = ZoneCalcHelper.genKey(srcZone, destZone);
        return bandwidthMap.get(key);
    }

    public Map<String, Long> getBandwidthMap() {
        return new HashMap<String, Long>(bandwidthMap);
    }

    public DeployLevel getLevel(){
        return level;
    }

}
