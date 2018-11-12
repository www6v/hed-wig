/**
 * 
 */
package com.yhd.arch.photon.common;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author root
 * 
 */
public class ProfileUtil {

	public static int validateWeight(final int weight) {
		int v = 1;
		v = weight < 0 ? v : weight;
		v = weight > InternalConstants.WEIGHT_LIMIT ? InternalConstants.WEIGHT_LIMIT : weight;
		return v;
	}

	public static HostInfo generateHostInfo(ServiceProfile profile) {
		HostInfo h = null;
		if (profile != null) {
			h = new HostInfo(profile);
		}
		return h;
	}
	public static String buildProfileKey(String name,String profileUUId){
		return name+"."+profileUUId;
	}
}
