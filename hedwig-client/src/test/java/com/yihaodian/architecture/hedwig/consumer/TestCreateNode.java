/**
 * 
 */
package com.yihaodian.architecture.hedwig.consumer;

import junit.framework.TestCase;

import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.architecture.zkclient.ZkClient;

/**
 * @author root
 *
 */
public class TestCreateNode extends TestCase {
	public static String PATH = "/TheStore/UnknowDomain/testService/0.1";
	public static String serverList = "192.168.35.16:2181";
	public static String hostIp;
	public static String CPATH = "";

	// public void testCreate() {
	// ZkClient _zkCLient = new ZkClient(serverList);
	// ServiceProfile bp = TestUtil.getServiceProfile();
	// _zkCLient.createPersistent(bp.getParentPath(), true);
	// try {
	// String path = ZkUtil.createChildPath(bp);
	// _zkCLient.createEphemeral(path, bp);
	// ServiceProfile rsp = _zkCLient.readData(path);
	// assertEquals(bp.getServiceUrl(), rsp.getServiceUrl());
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public void testSequencalNode() throws HedwigException, InterruptedException {
		ZkClient _zkCLient = new ZkClient(serverList,1,Integer.MAX_VALUE);
		_zkCLient.createPersistent("/test/test", true);
		for (int i = 0; i < 10; i++) {
			_zkCLient.createEphemeralSequential("/test/test/", SystemUtil.getLocalhostIp());
		}
		Thread.sleep(1000000);

	}
}
