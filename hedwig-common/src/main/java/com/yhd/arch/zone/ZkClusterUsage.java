package com.yhd.arch.zone;

public enum ZkClusterUsage {

	UNKNOWN(ZoneConstants.ZK_USAGE_UNKNOWN), SOA(ZoneConstants.ZK_USAGE_SOA), MQ(ZoneConstants.ZK_USAGE_MQ),
	CACHE(ZoneConstants.ZK_USAGE_CACHE), DAL(ZoneConstants.ZK_USAGE_DAL), SCHEDULER(ZoneConstants.ZK_USAGE_SCHEDULER),
	OPS(ZoneConstants.ZK_USAGE_OPS),EXT1(ZoneConstants.ZK_USAGE_EXT1),EXT2(ZoneConstants.ZK_USAGE_EXT2);

	private String code;

	private ZkClusterUsage(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static ZkClusterUsage getByCode(String c) {
		for (ZkClusterUsage usage : ZkClusterUsage.values()) {
			if (usage.getCode().equals(c)) {
				return usage;
			}
		}
		return UNKNOWN;
	}
}
