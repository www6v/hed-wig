/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.exception;

/**
 * @author root
 *
 */
public class InvalidParamException extends HedwigException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 22584697336399243L;

	public InvalidParamException() {
		super();
	}

	public InvalidParamException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidParamException(String message) {
		super(message);
	}

	public InvalidParamException(Throwable cause) {
		super(cause);
	}

}
