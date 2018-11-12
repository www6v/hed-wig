package com.yhd.arch.idc;

import com.yhd.arch.TestBase;
import com.yhd.arch.zone.Zone;
import com.yhd.arch.zone.ZoneContainer;
import com.yihaodian.architecture.zkclient.ZkClient;
import junit.framework.TestCase;

import java.util.Set;

/**
 * Created by root on 06/02/2017.
 */
public class TestIdcContainer extends TestBase {

    public void testGetAllIdc(){
        Set<String> idcs = ZoneContainer.getInstance().getIdcContainer().getAllIdc();
        for(String idc:idcs){
            System.out.println(idc);
        }
    }

    public void testIdcZones()
    {
        Set<String> idcs = ZoneContainer.getInstance().getIdcContainer().getAllIdc();
        for(String idc:idcs){
           Set<Zone> zones= ZoneContainer.getInstance().getIdcContainer().getIdcZones(idc);
            System.out.println(idc);
            for(Zone zone:zones){
                System.out.println("    "+zone.getName());
            }
        }
    }

    public void testGetZoneBrothers(){
        Set<Zone> zones = ZoneContainer.getInstance().getIdcContainer().getZoneBrothers("ZONE_NH");
        for(Zone zone:zones){
            System.out.println(zone.getName());
        }
    }

    public void testLocalZone(){
        System.out.println("local zone:"+ZoneContainer.getInstance().getLocalZoneName());
    }



    public void testGetLocalIdc(){
        System.out.println("local idc:"+ZoneContainer.getInstance().getIdcContainer().getLocalIdc());
    }
}
