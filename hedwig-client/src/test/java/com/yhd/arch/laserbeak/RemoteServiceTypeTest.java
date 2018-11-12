package com.yhd.arch.laserbeak;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yhd.arch.photon.constants.RemoteServiceType;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;

/**
 * @author wangbenwang
 *
 */
public class RemoteServiceTypeTest {

	private ZkClient zkClient;
	private String path = "/FlagsCenter/yihaodian#hedwig/RemoteServiceType";
	private static String envPath = "/Users/root/Applications/work/envConfig";
	private static Logger logger = LoggerFactory.getLogger(RemoteServiceTypeTest.class);

	@Before
	public void init() throws HedwigException {
		System.setProperty("global.config.path", envPath);
		System.setProperty("clientAppName", "root-win");

		zkClient = ZkUtil.getZkClientInstance();
		logger.info("ZK client has been iniialized.");
	}

	@After
	public void destroy() {
		this.zkClient.close();
		logger.info("ZK client has been closed.");
	}

	@Test
	public void testRemoveRemoteServiceType() {
		this.zkClient.delete(path);
		logger.info("The path[" + this.path + "] has been removed.");
	}

	@Test
	public void testModifyRemoteServiceType() {
		boolean exists = this.zkClient.exists(path);
		if (exists) {
			this.zkClient.writeData(path, RemoteServiceType.AKKA);
			logger.info("The data has been updated on the path[" + this.path + "]");
		} else {
			this.zkClient.createPersistent(path, true);
			logger.info("The path[" + this.path + "] has been created.");

			this.zkClient.writeData(path, RemoteServiceType.DEFAULT);
			logger.info("The data has been updated on the path[" + this.path + "]");
		}
	}

}
