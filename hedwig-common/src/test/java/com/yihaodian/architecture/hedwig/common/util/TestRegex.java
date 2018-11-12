package com.yihaodian.architecture.hedwig.common.util;

import junit.framework.TestCase;

public class TestRegex extends TestCase {

	public void testSalash() {
		String poolPattern = "\\w+/\\w+[-?\\w+]+";
		String s = "3d/2d";
		String s1 = "asdfasfd_sdfasdf";
		String s2 = "sadfasdf/asdfasdf-sdfdfd";
		String s3 = "asdfasd/asdfdf_sdfdf";
		String s4 = "asdfasdasdfdf_sdfdf";
		String s5 = "yihaodian/it5-ad-autoxp-task";
		assertTrue(s.matches(poolPattern));
		assertFalse(s1.matches(poolPattern));
		assertTrue(s2.matches(poolPattern));
		assertTrue(s3.matches(poolPattern));
		assertFalse(s4.matches(poolPattern));
		assertTrue(s5.matches(poolPattern));
	}
}
