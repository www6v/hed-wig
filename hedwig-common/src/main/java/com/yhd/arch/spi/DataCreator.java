/**
 * 
 */
package com.yhd.arch.spi;

import java.util.Random;

/**
 * @author root
 *
 */
public class DataCreator {
	static byte[] Bytesample;
	static {
		Bytesample = new byte[128];
		for (byte i = 0; i < 127; i++) {
			Bytesample[i] = i;
		}
	}

	/**
	 * @param size
	 *            unit is 'k'
	 * @return
	 */
	public static String createObject(int size) {
		byte[] ba = null;
		Random r = new Random();
		if (size > 0) {
			int realSize = size << 10;
			ba = new byte[realSize];
			for (int i = 0; i < realSize; i++) {
				int p = r.nextInt(127);
				ba[i] = Bytesample[p];
			}
		}
		return new String(ba);
	}

	public static String createByteObject(int size) {
		byte[] ba = null;
		Random r = new Random();
		if (size > 0) {
			ba = new byte[size];
			for (int i = 0; i < size; i++) {
				int p = r.nextInt(127);
				ba[i] = Bytesample[p];
			}
		}
		return new String(ba);
	}
}
