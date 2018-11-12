/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.constants;

/**
 * @author root
 *
 */
public enum ExceptionTypeConstants {

	NORMAL_MSG(1, "Hathaway Exception Normal:"),
 NORMAL_MSG_CAUSE(2, "Hathaway Exception Normal:");
	
	private int type;
	private String profix;
	
	ExceptionTypeConstants(int type, String profix) {
		this.type = type;
		this.profix = profix;
	}

	public int getType() {
		return type;
	}

	public String getProfix() {
		return profix;
	}

}
