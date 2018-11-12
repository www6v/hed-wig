package com.yhd.arch.idc;

import com.yhd.arch.zone.Zone;

import java.util.*;

/**
 * Created by root on 06/02/2017.
 */
public class Idc {
    private String name;
    private String zoneName;
    private Map<String,Zone> zoneMap= new HashMap<String,Zone>();


    public Idc(String idcId) {
        name = idcId;
    }

    public void putZone(Zone zone){
        zoneMap.put(zone.getName(),zone);
    }

    public Set<Zone> getZones(){
        return new HashSet<Zone>(zoneMap.values());
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
}
