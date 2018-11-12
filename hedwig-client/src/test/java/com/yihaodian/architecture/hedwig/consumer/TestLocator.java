/**
 * 
 */
package com.yihaodian.architecture.hedwig.consumer;

import java.util.Collection;

import junit.framework.TestCase;

import com.yihaodian.architecture.hedwig.client.locator.ZkServiceLocator;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;

/**
 * @author root
 *
 */
public class TestLocator extends TestCase {

	public void testLoadServiceProfile() throws HedwigException {
		ClientProfile profile = TestUtil.getClientProfile();
		ZkServiceLocator loactor = new ZkServiceLocator(profile);
		
		 try {
			while (true) {
			Collection<ServiceProfile> map = loactor.getAllService();
			System.out.println(map);
			Thread.currentThread().sleep(5000);
			}
		
		 } catch (InterruptedException e) {
		 e.printStackTrace();
		 }
	}
}
