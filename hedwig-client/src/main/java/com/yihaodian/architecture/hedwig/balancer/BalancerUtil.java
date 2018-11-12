/**
 * 
 */
package com.yihaodian.architecture.hedwig.balancer;

import java.util.ArrayList;
import java.util.Collection;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;

/**
 * @author root
 * 
 */
public class BalancerUtil {
	public static Collection<ServiceProfile> filte(Collection<ServiceProfile> profiles, Collection<String> whiteList) {
		Collection<ServiceProfile> groupedProfiles = new ArrayList<ServiceProfile>();
		if (whiteList == null) {
			groupedProfiles = profiles;
		} else {
			if (whiteList.size() > 0) {
				for (ServiceProfile sp : profiles) {
					try {
						String process = ZkUtil.getProcessDesc(sp);
						if (whiteList.contains(process)) {
							groupedProfiles.add(sp);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return groupedProfiles;
	}
}
