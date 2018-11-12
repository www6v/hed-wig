/**
 * 
 */
package com.yihaodian.architecture.hedwig.provider;

import junit.framework.TestCase;

import com.yhd.arch.laserbeak.register.CentralizeRegister;
import com.yhd.arch.photon.constants.ProtocolType;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.register.ServiceProviderZkRegister;

/**
 * @author root
 * 
 */
public class TestRegister extends TestCase {

	public void testRegist() throws HedwigException {
		ServiceProviderZkRegister register = new ServiceProviderZkRegister();
		ServiceProfile sp = new ServiceProfile();
		try {
			register.regist(sp);
			Thread.currentThread().sleep(500000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testCentralRegister() throws HedwigException, InterruptedException {
		CentralizeRegister cr = new CentralizeRegister();
		ServiceProfile sp = new ServiceProfile();
		sp.setDomainName("601");
		sp.setServiceAppName("laserServer");
		sp.setPort(20001);
		sp.setTransProtocol(ProtocolType.AKKAtcp);
		sp.getServiceUrl();
		cr.regist(sp);
		Thread.currentThread().sleep(500000);
	}
}
