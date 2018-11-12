/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.uuid;

import junit.framework.TestCase;

/**
 * @author root
 *
 */
public class TestUUID extends TestCase {

	public void testUUIDGenerator() {
		long start = System.nanoTime();
		for (long i = 0; i < Long.MAX_VALUE; i++) {
			if (i % 1000000 == 0) {
				System.out.println(new UUID().toString());
			}
		}
		System.out.println((System.nanoTime() - start) / Long.MAX_VALUE);

	}
}
