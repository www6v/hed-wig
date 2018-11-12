/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;

/**
 * @author root
 * 
 */
public class ServiceRelivePolicy implements RelivePolicy {

	Logger logger = LoggerFactory.getLogger(ServiceRelivePolicy.class);
	private String providerHost = "";
	private static final int DEFAULT_RELIVE_INTERVAL = 500;
	private volatile int tryCount = 0;
	private volatile int threshold = InternalConstants.DEFAULT_RELIVE_THRESHOLD;
	private int SCALE = 2;
	private int COUNT_LIMIT = 60000;
	private volatile long start = 0;
	private volatile long interval = DEFAULT_RELIVE_INTERVAL;
	private long TIME_LIMIT = 60000;

	public ServiceRelivePolicy(String providerHost) {
		super();
		this.providerHost = providerHost;
	}

	public ServiceRelivePolicy() {
		super();
	}

	@Override
	public boolean tryRelive() {
		boolean value = false;
		boolean vc = meetCountPolicy();
		boolean vt = meetTimePolicy();
		if (vc || vt) {
			interval = interval * SCALE;
			interval = interval < TIME_LIMIT ? interval : TIME_LIMIT;
			threshold = threshold * SCALE;
			threshold = threshold < COUNT_LIMIT ? threshold : COUNT_LIMIT;
			value = true;
		}
		return value;
	}

	private boolean meetTimePolicy() {
		boolean v = false;
		if (start == 0) {
			start = System.currentTimeMillis();
		} else {
			long tmp = System.currentTimeMillis() - start;
			if (tmp > interval) {
				start = System.currentTimeMillis();
				v = true;
				logger.warn(InternalConstants.LOG_PROFIX + providerHost + " node revive due to meetTimePolicy:" + interval + "ms");
			}
		}
		return v;
	}

	private boolean meetCountPolicy() {
		boolean v = false;
		tryCount++;
		if (tryCount >= threshold) {
			tryCount = 0;
			v = true;
			logger.warn(InternalConstants.LOG_PROFIX + providerHost + " node revive due to meetCountPolicy:" + threshold);
		}
		return v;
	}

	@Override
	public void reset() {
		tryCount = 0;
		threshold = InternalConstants.DEFAULT_RELIVE_THRESHOLD;
		start = 0;
		interval = DEFAULT_RELIVE_INTERVAL;
	}

	public String getProviderHost() {
		return providerHost;
	}

	public void setProviderHost(String providerHost) {
		this.providerHost = providerHost;
	}
}
