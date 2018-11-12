/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.constants;

/**
 * @author root
 *
 */
public enum ServiceStatus {

	ENABLE(1), DISENABLE(-1), TEMPORARY_DISENABLE(0);
	private int code;

	private ServiceStatus(int code) {
		this.setCode(code);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}


}
