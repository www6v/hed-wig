/**
 * 
 */
package com.yhd.laserbeak.provider;

import com.yhd.arch.TestBase;
import com.yhd.arch.laserbeak.register.ZoneProfileRegister;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;

/**
 * @author root
 *
 */
public class TestZoneeProfileRegister extends TestBase {

	public void testRegist() throws InterruptedException {
		ServiceProfile sp = new ServiceProfile();
		sp.setServiceAppName("rootAppName");
		sp.setServiceName("rootService");
		sp.setDomainName("rootDomain");
		sp.setServiceVersion("1-zone");
		ZoneProfileRegister zpr = new ZoneProfileRegister();
		try {
			long start = System.currentTimeMillis();
			zpr.regist(sp);
			System.out.println("regist1 cost:" + (System.currentTimeMillis() - start));
		} catch (InvalidParamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.sleep(100000000);
		// ZoneProfileRegister zpr1 = new ZoneProfileRegister();
		// try {
		// long start = System.currentTimeMillis();
		// zpr1.regist(sp);
		// System.out.println("regist2 cost:" + (System.currentTimeMillis() -
		// start));
		// } catch (InvalidParamException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
