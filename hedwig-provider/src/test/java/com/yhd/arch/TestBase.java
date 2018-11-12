package com.yhd.arch;

import junit.framework.TestCase;

public class TestBase extends TestCase {

	@Override
	protected void setUp() throws Exception {
		//System.setProperty("global.config.path", "/Users/root/Applications/work/envConfig");
		System.setProperty("global.config.path", "C:\\root\\envConfig");
		super.setUp();
	}

}
