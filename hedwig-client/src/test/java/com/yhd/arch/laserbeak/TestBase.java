package com.yhd.arch.laserbeak;

import com.yhd.arch.spi.DataCreator;
import com.yihaodian.architecture.hedwig.hessian.client.Parameters;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class TestBase extends TestCase {

	protected static String s = DataCreator.createByteObject(50);
	private String envPath = "/Users/root/Applications/work/envConfig";

	@Override
	protected void setUp() throws Exception {
		System.setProperty("global.config.path", envPath);
		System.setProperty("clientAppName", "root");
		super.setUp();
	}

	protected static Parameters createParam() {
		Parameters p = new Parameters();
		// p.setStrParam("Say hi");
		// p.setIntParam(1);
		Map<Object, Object> m = new HashMap<Object, Object>();
		// m.put("1", new Integer(11));
		// m.put("2", 2);
		// m.put("3", new BigDecimal(3.1415926));
		// BigDecimal b = new BigDecimal(0.0000001415926);
		// m.put("4", b);
		// m.put("5", new Date(1369900568611L));
		// m.put("6", new MpViolationTermRpc());
		m.put("7", s);
		p.setMap(m);
		return p;
	}
}
