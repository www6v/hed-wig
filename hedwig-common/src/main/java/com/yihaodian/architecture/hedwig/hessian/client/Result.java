/**
 * 
 */
package com.yihaodian.architecture.hedwig.hessian.client;

import java.io.Serializable;

/**
 * @author jianglie
 *
 */
public class Result<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7901136346273461599L;
	private boolean success;
	private T value;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
