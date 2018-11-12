package com.yhd.laserbeak.provider;

import java.util.ArrayList;
import java.util.List;

import com.yhd.arch.TestBase;
import com.yhd.arch.photon.util.ActorNameUtil;
import com.yhd.arch.zone.RoutePriority;
import com.yhd.arch.zone.ZoneConstants;
import com.yhd.arch.zone.bean.MethodCrossZoneInfo;
import com.yhd.arch.zone.bean.ServiceCrossZoneInfo;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;

public class TestZoneProvider extends TestBase {

	private String method1 = "delete";
	private String method2 = "insert_Parameter_string_int";
	private String method3 = "query_string";
	private String method4 = "update_Parameter";
	private String sFlagPath = InternalConstants.BASE_ROOT_FLAGS + "/" + TestConstant.EXAMPLE_APP_NAME + ZoneConstants.FLAG_CROSS_ZONE;

	public void testGetMethodName() {
		System.out.println(ActorNameUtil.getMethodNames(TestConstant.EXAMPLE_SERVICE_NAME_BASE, IExampleInterface.class));
	}

	public void testWriteCrossZoneInfo() {
		ServiceCrossZoneInfo sczi = new ServiceCrossZoneInfo(TestConstant.EXAMPLE_SERVICE_NAME_BASE);
		MethodCrossZoneInfo m1cziBj = new MethodCrossZoneInfo(method1, TestConstant.ZONE_BJ, RoutePriority.Default.getCode());
		MethodCrossZoneInfo m1cziJQ = new MethodCrossZoneInfo(method1, TestConstant.ZONE_JQ, RoutePriority.Primary.getCode());
		MethodCrossZoneInfo m1cziNH = new MethodCrossZoneInfo(method1, TestConstant.ZONE_NH, RoutePriority.None.getCode());
		List<MethodCrossZoneInfo> l1 = new ArrayList<MethodCrossZoneInfo>();
		l1.add(m1cziJQ);
		l1.add(m1cziBj);
		l1.add(m1cziNH);
		sczi.setMethodCrossZoneInfo(method1, l1);

		MethodCrossZoneInfo m2cziBj = new MethodCrossZoneInfo(method2, TestConstant.ZONE_BJ, RoutePriority.Default.getCode());
		MethodCrossZoneInfo m2cziJQ = new MethodCrossZoneInfo(method2, TestConstant.ZONE_JQ, RoutePriority.None.getCode());
		MethodCrossZoneInfo m2cziNH = new MethodCrossZoneInfo(method2, TestConstant.ZONE_NH, RoutePriority.None.getCode());
		List<MethodCrossZoneInfo> l2 = new ArrayList<MethodCrossZoneInfo>();
		l2.add(m2cziJQ);
		l2.add(m2cziBj);
		l2.add(m2cziNH);
		sczi.setMethodCrossZoneInfo(method2, l2);

		ServiceCrossZoneInfo sczi2 = new ServiceCrossZoneInfo(TestConstant.EXAMPLE_SERVICE_NAME_QUERY);
		MethodCrossZoneInfo m1cziBj2 = new MethodCrossZoneInfo(method3, TestConstant.ZONE_BJ, RoutePriority.Default.getCode());
		MethodCrossZoneInfo m1cziJQ2 = new MethodCrossZoneInfo(method3, TestConstant.ZONE_JQ, RoutePriority.Primary.getCode());
		MethodCrossZoneInfo m1cziNH2 = new MethodCrossZoneInfo(method3, TestConstant.ZONE_NH, RoutePriority.None.getCode());
		List<MethodCrossZoneInfo> s2l1 = new ArrayList<MethodCrossZoneInfo>();
		s2l1.add(m1cziJQ2);
		s2l1.add(m1cziBj2);
		s2l1.add(m1cziNH2);
		sczi2.setMethodCrossZoneInfo(method3, s2l1);

		MethodCrossZoneInfo m2cziBj3 = new MethodCrossZoneInfo(method4, TestConstant.ZONE_BJ, RoutePriority.Default.getCode());
		MethodCrossZoneInfo m2cziJQ3 = new MethodCrossZoneInfo(method4, TestConstant.ZONE_JQ, RoutePriority.None.getCode());
		MethodCrossZoneInfo m2cziNH3 = new MethodCrossZoneInfo(method4, TestConstant.ZONE_NH, RoutePriority.None.getCode());
		List<MethodCrossZoneInfo> s2l2 = new ArrayList<MethodCrossZoneInfo>();
		s2l2.add(m2cziJQ3);
		s2l2.add(m2cziBj3);
		s2l2.add(m2cziNH3);
		sczi2.setMethodCrossZoneInfo(method4, s2l2);
		try {
			String p1 = sFlagPath + "/" + TestConstant.EXAMPLE_SERVICE_NAME_BASE;
			String p2 = sFlagPath + "/" + TestConstant.EXAMPLE_SERVICE_NAME_QUERY;
			ZkUtil.getZkClientInstance().writeData(p1, sczi);
			ZkUtil.getZkClientInstance().createPersistent(p2, true);
			ZkUtil.getZkClientInstance().writeData(p2, sczi2);
		} catch (HedwigException e) {
			e.printStackTrace();
		}
	}
}
