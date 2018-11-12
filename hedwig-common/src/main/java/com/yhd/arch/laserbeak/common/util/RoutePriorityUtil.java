package com.yhd.arch.laserbeak.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yhd.arch.zone.RoutePriority;

public class RoutePriorityUtil {

	public static Map<String, RoutePriority> createDefaultMethodRP(List<String> mList) {
		Map<String, RoutePriority> methodRP = new HashMap<String, RoutePriority>();
		if (mList != null && mList.size() > 0) {
			for (String mn : mList) {
				methodRP.put(mn, RoutePriority.Default);
			}
		}
		return methodRP;
	}
}
