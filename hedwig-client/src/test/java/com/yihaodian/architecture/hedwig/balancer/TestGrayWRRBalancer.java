/**
 * 
 */
package com.yihaodian.architecture.hedwig.balancer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import com.yihaodian.architecture.hedwig.balancer.special.GrayInfo;
import com.yihaodian.architecture.hedwig.balancer.special.GrayWRRBalancer;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author root
 * 
 */
public class TestGrayWRRBalancer extends TestCase {

	HashSet<ServiceProfile> spSet = new HashSet<ServiceProfile>();
	List<String> whiteList = new ArrayList<String>();
	List<String> grayList = new ArrayList<String>();

	@Override
	protected void setUp() throws Exception {
		System.setProperty("global.config.path", "D:\\root\\lunaWorkspace\\test\\client\\src\\test\\resources");
		Random r = new Random();
		for (int i = 0; i < 50; i++) {
			ServiceProfile sp = new ServiceProfile();
			int w = r.nextInt(5);
			w = w == 0 ? w = 1 : w;
			sp.setWeighted(2);
			sp.setHostIp("192.168.1." + i);
			sp.setPort(80);
			sp.getServiceUrl();
			spSet.add(sp);
		}
		for (int i = 0; i < 50; i += 2) {
			String s = "192.168.1." + i + ":80";
			whiteList.add(s);
		}

		for (int i = 0; i < 25; i++) {
			String s = "192.168.1." + i;
			grayList.add(s);
		}

		super.setUp();
	}

	public void testBase() {
		GrayWRRBalancer gwrr = new GrayWRRBalancer();
		gwrr.updateProfiles(spSet);
		while (true) {
			System.out.println(gwrr.select().getHostString());
		}
	}

	public void testWhiteList() {
		GrayWRRBalancer gwrr = new GrayWRRBalancer();
		gwrr.setWhiteList(whiteList);
		gwrr.updateProfiles(spSet);
		while (true) {
			System.out.println(gwrr.select().getHostString());
		}
	}

	public void testGray() {
		System.out.println(grayList);
		GrayWRRBalancer gwrr = new GrayWRRBalancer();
		gwrr.setWhiteList(whiteList);
		GrayInfo gi = new GrayInfo();
		gi.setGraySet(grayList);
		gi.setStart(1000);
		gwrr.setSpecialInfo(gi);
		gwrr.updateProfiles(spSet);
		for (int i = 0; i < 1000; i++) {
			System.out.println(gwrr.select(4 + "").getHostString());
		}
	}
}
