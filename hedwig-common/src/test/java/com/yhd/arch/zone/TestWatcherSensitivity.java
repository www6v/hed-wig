package com.yhd.arch.zone;

import java.util.List;

import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.ZkClient;

public class TestWatcherSensitivity {

	public static String path = "/HedwigTest";

	public static void main(String[] args) {
		try {
			System.setProperty("global.config.path", "D:\\root\\cloudDriver\\source\\work");
			ZkClient zkClient = ZkUtil.getZkClientInstance();
			zkClient.createPersistent(path, true);
			zkClient.subscribeChildChanges(path, new IZkChildListener() {

				@Override
				public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
					System.out.println(currentChilds.size());
				}
			});
			int interval = 50;
			String epath = path + "/aaaaa";
			Thread.sleep(interval);
			zkClient.createEphemeral(epath);
			Thread.sleep(interval);
			zkClient.delete(epath);
			Thread.sleep(interval);
			zkClient.createEphemeral(epath);
			Thread.sleep(interval);
			zkClient.delete(epath);
			Thread.sleep(10 * interval);
		} catch (HedwigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
