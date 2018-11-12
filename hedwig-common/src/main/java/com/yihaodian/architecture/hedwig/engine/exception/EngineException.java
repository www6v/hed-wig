/**
 * 
 */
package com.yihaodian.architecture.hedwig.engine.exception;

import com.yihaodian.architecture.hedwig.common.exception.HedwigException;

/**
 * @author root
 *
 */
public class EngineException extends HedwigException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3997749543314145590L;

	public EngineException() {
		super();
	}

	public EngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public EngineException(String message) {
		super(message);
	}

	public EngineException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

}
