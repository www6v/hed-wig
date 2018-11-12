/**
 * 
 */
package com.yhd.laserbeak.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Laserbeak server for upgrading.
 * 
 * @author wangbenwang
 *
 */
public class LaserServer4Upgrade {
	private static String envPath = "/Users/root/Applications/work/envConfig";

	public static void main(String[] args) throws Exception {
		System.setProperty("global.config.path", envPath);
		final ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "laserbeak-server-upgrade.xml" });
	}
}
