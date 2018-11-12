/**
 * 
 */
package com.yhd.laserbeak.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yihaodian.architecture.hedwig.common.exception.HedwigException;

/**
 * @author root
 * 
 */
public class Server1 {
	public static void main(String[] args) throws HedwigException {
		new ClassPathXmlApplicationContext(new String[] { "laserbeak-server.xml" });
	}
}
