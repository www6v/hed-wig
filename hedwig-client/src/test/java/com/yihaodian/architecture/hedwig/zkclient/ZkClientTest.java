package com.yihaodian.architecture.hedwig.zkclient;

import org.apache.zookeeper.KeeperException.NodeExistsException;

import com.yhd.arch.photon.constants.ProtocolType;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.architecture.zkclient.exception.ZkNodeExistsException;

public class ZkClientTest {
	public static void main(String[] args) {
		ZkClient zkClient = null;
		try {
			zkClient = ZkUtil.getZkClientInstance();
		} catch (HedwigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String parentPath = "/TheStore/testWW/105-frontend-GPS_GSS/GPS-BUSYSTOCK/busyCacheAdminFacadeService_AKKA/3.1.0-1";
        zkClient.createPersistent(parentPath,true);
        
		String path = parentPath + "/10.4.6.115:1920";
		
		for (int i = 0; i < 9000; i++) {
    		String zkPath = path + i;
			ServiceProfile sp = new ServiceProfile();			
			sp.setDomainName("601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601601");
			sp.setServiceAppName("laserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServerlaserServer");
			sp.setPort(20001);
			sp.setTransProtocol(ProtocolType.AKKAtcp);
			//sp.getServiceUrl();
			
			try{
				zkClient.createEphemeral(zkPath, sp);		
			} catch(ZkNodeExistsException nee) {
				System.out.println( zkPath + "exists ");
			}
			
			System.out.println("createEphemeral:" + zkPath);
		}
		
        for (int i = 0; i < 9000; i++) {
    		String zkPath = path + i;
			zkClient.subscribeDataChanges(zkPath, new IZkDataListener() {
    			@Override
    			public void handleDataChange(String dataPath, Object data) throws Exception {
    				System.out.println("handleDataChange:" + dataPath);
//    				ZoneContainer.getInstance().initZones();
    			}

    			@Override
    			public void handleDataDeleted(String dataPath) throws Exception {
//    				ZoneContainer.getInstance().initZones();
    				System.out.println("handleDataDeleted:" + dataPath);
    			}
    		});	
			
			System.out.println("subscribeDataChanges:" + zkPath);
		}

        try {
			Thread.sleep(1000000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
