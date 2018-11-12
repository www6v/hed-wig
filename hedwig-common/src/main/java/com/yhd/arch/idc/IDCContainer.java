package com.yhd.arch.idc;

import com.yhd.arch.zone.DeployLevel;
import com.yhd.arch.zone.Zone;
import com.yhd.arch.zone.ZoneConstants;
import com.yhd.arch.zone.ZoneContainer;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by root on 06/02/2017.
 */
public class IDCContainer {
    private static Logger logger = LoggerFactory.getLogger(IDCContainer.class);
    private Map<String,String> zoneIdcMap;
    private Map<String,Idc> idcMap;
    private String localIdc;
    private Lock lock = new ReentrantLock();

    public IDCContainer(Collection<Zone> zones)
    {
        refresh(zones);
        localIdc = ProperitesContainer.provider().getProperty(PropKeyConstants.LOCAL_IDC_NAME);
    }

    public void refresh(Collection<Zone> zones){
        if(zones!=null){
            Map<String,String> newzoneIdcMap = new HashMap<String, String>();
            Map<String,Idc> newidcMap = new HashMap<String, Idc>();
            for(Zone zone:zones){
                String zoneId = zone.getName();
                String idcId = zone.getPlatform();
                newzoneIdcMap.put(zoneId,idcId);
                Idc idc;
                if(!newidcMap.containsKey(idcId))
                {
                    idc = new Idc(idcId);
                    newidcMap.put(idcId,idc);
                }else{
                    idc=newidcMap.get(idcId);
                }
                idc.putZone(zone);
                if(DeployLevel.IDC.equals(zone.getZoneLevel())){
                    idc.setZoneName(zone.getName());
                }
            }
            zoneIdcMap = newzoneIdcMap;
            idcMap = newidcMap;
        }
    }

    public Set<Zone> getIdcZones(String idcId){
        Set<Zone> zones = null;
        if(!HedwigUtil.isBlankString(idcId)){
            Idc idc = idcMap.get(idcId);
            if(idc!=null){
                zones = idc.getZones();
            }
        }
        return zones;
    }

    public Set<Zone> getZoneBrothers(String zoneId){
        Set<Zone> zones = null;
        String idcId = zoneIdcMap.get(zoneId);
        if(!HedwigUtil.isBlankString(idcId)){
            zones =getIdcZones(idcId);
        }
        return zones;
    }

    public Set<String> getAllIdc(){
        return idcMap.keySet();
    }

    public boolean isSameIdc(String zoneId1,String zoneId2){
        String idcId1 = zoneIdcMap.get(zoneId1);
        String idcId2 = zoneIdcMap.get(zoneId2);
        return idcId1.equals(idcId2);
    }

    public String getLocalIdc(){
        return localIdc;
    }

    public String getIdcZoneName(String idcId){
        String name = "";
        Idc idc = idcMap.get(idcId);
        if(idc!=null){
            name = idc.getZoneName();
        }
        return name;
    }
}
