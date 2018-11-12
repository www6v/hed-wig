/**
 * 
 */
package com.yhd.arch.zone;

import java.util.Map;

import com.yhd.arch.TestBase;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * @author root
 *
 */
public class TestZoneContainer extends TestBase {

	public void testBandwidth() {
		Map<String, Long> map = ZoneContainer.getInstance().getBandwidthMap();
		map.put("ZONE_NH#ZONE_BJ", 100l);
		ZkClient zclient = ZoneContainer.getInstance().getLocalZkClient(ZkClusterUsage.SOA);
		System.out.println(ZoneContainer.getInstance().getBandwidthMap());
		System.out.println(ZoneContainer.getInstance().getDistence("ZONE_NH", "ZONE_BJ"));
		System.out.println(ZoneContainer.getInstance().getAllZoneName());
		System.out.println("level:"+ZoneContainer.getInstance().getLevel());
		System.out.println(ZoneContainer.getInstance().getIdcContainer().getIdcZoneName("BJ"));
	}

	public void testZoneName(){
		/*
		String path = ZoneConstants.ZONE_ROOT+"/zones/ZONE_BJ";
		Zone z= zclient.readData(path);
		z.setZoneLevel(DeployLevel.IDC);
		zclient.writeData(path, z);
		path = ZoneConstants.ZONE_ROOT+"/zones/ZONE_JQ";
		Zone z1= zclient.readData(path);
		z1.setZoneLevel(DeployLevel.IDC);
		zclient.writeData(path, z1);
		System.out.println(z);*/
	}
}
