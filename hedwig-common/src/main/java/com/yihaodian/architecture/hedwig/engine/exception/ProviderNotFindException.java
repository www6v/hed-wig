/**
 * 
 */
package com.yihaodian.architecture.hedwig.engine.exception;


/**
 * @author root
 *
 */
public class ProviderNotFindException extends HandlerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8290861787325999884L;

	public ProviderNotFindException() {
		super();
	}

	public ProviderNotFindException(String reqId, String requestId, String message) {
		super(reqId, requestId, message);
	}

	public ProviderNotFindException(String reqId, String message, Throwable cause) {
		super(reqId, message, cause);
	}

	public ProviderNotFindException(String reqId, String message) {
		super(reqId, message);
	}

	public ProviderNotFindException(String reqId, Throwable cause) {
		super(reqId, cause);
	}

}
