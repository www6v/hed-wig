/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author root
 *
 */
public class HedwigAssert {
	private static Logger logger = LoggerFactory.getLogger(HedwigAssert.class);

	public static void isNull(Object object, String message) {
		if (object == null) {
			logger.error(message);
		}
	}
}
