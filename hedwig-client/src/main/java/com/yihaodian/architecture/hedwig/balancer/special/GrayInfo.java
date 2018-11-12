/**
 * 
 */
package com.yihaodian.architecture.hedwig.balancer.special;

import java.util.Collection;
import java.util.HashSet;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;

/**
 * @author root
 * 
 */
public class GrayInfo implements ISpecialInfo {

	private String publishId;
	private Collection<String> graySet;
	private int start;
	private int end;

	public Collection<String> getGraySet() {
		return graySet;
	}

	public void setGraySet(Collection<String> graySet) {
		this.graySet = graySet;
	}

	public String getPublishId() {
		publishId = HedwigUtil.isBlankString(publishId) ? "UnknowPublishId" : publishId;
		return publishId;
	}

	public void setPublishId(String publishId) {
		this.publishId = publishId;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Use bit to represent error state 1: publishId is null 10:offset error
	 * 100:ip error
	 * 
	 * @return
	 */
	public int getValidateCode() {
		int v = 0;
		if (HedwigUtil.isBlankString(publishId)) {
			v = 1;
		}
		if (start < 0 || start > InternalConstants.DEFAULT_MAX_TOKEN) {
			v = 1 << 1 | v;
		}
		if (graySet == null || graySet.size() == 0) {
			v = 1 << 2 | v;
		}
		return v;
	}

	public void setGraySet(String ips) {
		if (!HedwigUtil.isBlankString(ips)) {
			if (ips.contains(",")) {
				String[] ipArr = ips.split(",");
				graySet = new HashSet<String>();
				for (String ip : ipArr) {
					if (StringUtils.isIpAddress(ip) || StringUtils.isIpPortAddress(ip)) {
						graySet.add(ip);
					}
				}
			} else {
				if (StringUtils.isIpAddress(ips) || StringUtils.isIpPortAddress(ips)) {
					graySet = new HashSet<String>();
					graySet.add(ips);
				}
			}

		}
	}

	public void setRange(String range) {
		if (!HedwigUtil.isBlankString(range)) {
			if (range.contains(",")) {
				String[] sArr = range.split(",");
				if (sArr != null) {
					if (sArr.length > 1) {
						end = StringUtils.isNumeric(sArr[1]) ? Integer.valueOf(sArr[1]) : 0;
					}
					start = StringUtils.isNumeric(sArr[0]) ? Integer.valueOf(sArr[0]) : 0;
				}
			} else {
				start = StringUtils.isNumeric(range) ? Integer.valueOf(range) : 0;
			}
		}
	}

	@Override
	public String toString() {
		return "GrayInfo [publishId=" + publishId + ", graySet=" + graySet + ", start=" + start + ", end=" + end + "]";
	}

}
