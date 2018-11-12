/**
 * 
 */
package com.yhd.arch.zone;

import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;
import junit.framework.TestCase;
import org.apache.zookeeper.data.Stat;

/**
 * @author root
 *
 */
public class ZoneTest extends TestCase {

	ZkClient client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		client = ZkUtil.getZkClientInstance();
	}

	public void testWriteZone() {
        Zone z1 = new Zone(TestConstant.ZONE_JQ, "JinQiao", "IDC", "IDC_SH", "ddddddddddddddd");
        z1.getZkClusterMap().put(ZkClusterUsage.SOA, "192.168.8.34:2181,192.168.8.35:2181,192.168.8.36:2181");
		z1.getZkClusterMap().put(ZkClusterUsage.CACHE, "192.168.8.34:2181,192.168.8.35:2181,192.168.8.36:2181");
        Zone z2 = new Zone(TestConstant.ZONE_BJ, "Beijing", "IDC", "IDC_BJ", "ddddddddddddddd");
        z2.getZkClusterMap().put(ZkClusterUsage.SOA, "192.168.8.28:2181,192.168.8.37:2181,192.168.8.39:2181");
		z1.getZkClusterMap().put(ZkClusterUsage.CACHE, "192.168.8.28:2181,192.168.8.37:2181,192.168.8.39:2181");
        Zone z3 = new Zone(TestConstant.ZONE_NH, "Nanhui", "IDC", "IDC_SH", "ddddddddddddddd");
        z3.getZkClusterMap().put(ZkClusterUsage.SOA, "10.161.144.77:2181,10.161.144.78:2181,10.161.144.79:2181");
		z1.getZkClusterMap().put(ZkClusterUsage.CACHE, "10.161.144.77:2181,10.161.144.78:2181,10.161.144.79:2181");
		writeZone(z1);
		writeZone(z2);
		writeZone(z3);
	}

	private void writeZone(Zone zone) {
		String path = ZoneConstants.ZONE_PATH + "/" + zone.getName();
		if (!client.exists(path)) {
			client.createPersistent(path, true);
		}
		Stat s = client.writeData(path, zone);
		System.out.println(s.getDataLength());
	}

	public void testContainer() {
        System.out.println("soaZk" + ZoneContainer.getInstance().getZkClient(TestConstant.ZONE_JQ, ZkClusterUsage.SOA));
        System.out.println("kiraZk" + ZoneContainer.getInstance().getZkClient(TestConstant.ZONE_JQ, ZkClusterUsage.SCHEDULER));
    }

	public void testWriteLocalZoneName() {
        client.writeData(ZoneConstants.ZONE_ROOT, TestConstant.ZONE_NH);
    }

	public void testGetLocalZone() {
		String l = ZoneContainer.getInstance().getLocalZoneName();
		System.out.println(l);
		// ZoneContainer.getInstance().getZkClient(l, ZkClusterUsage.SOA);
	}

	public void testGetAllZone() {
		System.out.println(ZoneContainer.getInstance().getAllZoneName());
	}
}
