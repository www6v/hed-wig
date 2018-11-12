/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.exception;

/**
 * @author root
 * 
 */
public class AuthenticationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 45633509332177580L;

	public AuthenticationException() {
		super();
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthenticationException(String message) {
		super(message);
	}

}
