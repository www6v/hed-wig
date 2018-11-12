/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author root
 * 
 */
public class StringUtils {
	private static Pattern numPattern = Pattern.compile("-?[0-9]*");
	private static Pattern ipPattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	private static Pattern ipPortPattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5]):(\\d{1,5})$");

	private static String poolIdPattern = "\\w+/\\w+[-?\\w+]+";

	public static boolean isPoolid(String str) {
		boolean b = false;
		if (!HedwigUtil.isBlankString(str)) {
			b = str.matches(poolIdPattern);
		}
		return b;
	}

	public static boolean isNumeric(String str) {
		boolean b = false;
		if (!HedwigUtil.isBlankString(str)) {
			Matcher isNum = numPattern.matcher(str.trim());
			if (isNum.matches()) {
				b = true;
			}
		}
		return b;
	}

	public static boolean isIpAddress(String str) {
		boolean b = false;
		if (!HedwigUtil.isBlankString(str)) {
			Matcher isIp = ipPattern.matcher(str);
			if (isIp.matches()) {
				b = true;
			}
		}
		return b;
	}

	public static boolean isIpPortAddress(String str) {
		boolean b = false;
		if (!HedwigUtil.isBlankString(str)) {
			Matcher isIpPort = ipPortPattern.matcher(str);
			if (isIpPort.matches()) {
				b = true;
			}
		}
		return b;
	}

	public static String addressInt2Ip4(int addInt) {
		int address = addInt;
		byte[] addr = new byte[4];

		addr[0] = (byte) ((address >>> 24) & 0xFF);
		addr[1] = (byte) ((address >>> 16) & 0xFF);
		addr[2] = (byte) ((address >>> 8) & 0xFF);
		addr[3] = (byte) (address & 0xFF);
		return numericToTextFormat(addr);
	}

	public static String numericToTextFormat(byte[] src) {
		return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
	}

	public static String replaceSlash(String value) {
		String v = null;
		if (!HedwigUtil.isBlankString(value)) {
			v = value.replace("/", "#");
		}
		return v;
	}

}
