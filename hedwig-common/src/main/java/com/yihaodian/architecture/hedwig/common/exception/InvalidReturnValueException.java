/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.exception;

/**
 * @author root
 *
 */
public class InvalidReturnValueException extends HedwigException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4991660928353116776L;

	public InvalidReturnValueException() {
		super();
	}

	public InvalidReturnValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidReturnValueException(String message) {
		super(message);
	}

	public InvalidReturnValueException(Throwable cause) {
		super(cause);
	}

}
