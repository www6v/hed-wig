package com.yihaodian.architecture.hedwig.common.constants;

public enum ExternalFlag {

	NONE(0), GRAY(1), ABTEST(2);

	private int code;

	ExternalFlag(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ExternalFlag getFlagByCode(int code) {
		ExternalFlag rf = ExternalFlag.NONE;
		for (ExternalFlag flg : ExternalFlag.values()) {
			if (flg.getCode() == code) {
				rf = flg;
			}
		}
		return rf;
	}
}
