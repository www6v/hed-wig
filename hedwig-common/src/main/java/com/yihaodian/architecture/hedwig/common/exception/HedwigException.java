/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.exception;



/**
 * @author root
 *
 */
public class HedwigException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8037986320070854409L;

	public HedwigException() {
		super();
	}

	public HedwigException(String message, Throwable cause) {
		super(message, cause);
	}

	public HedwigException(String message) {
		super(message);
	}

	public HedwigException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}
