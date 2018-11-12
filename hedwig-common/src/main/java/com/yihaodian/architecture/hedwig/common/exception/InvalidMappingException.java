package com.yihaodian.architecture.hedwig.common.exception;

public class InvalidMappingException extends HedwigException {

	private static final long serialVersionUID = 1L;

	public InvalidMappingException() {
		super();
	}

	public InvalidMappingException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidMappingException(String message) {
		super(message);
	}

	public InvalidMappingException(Throwable cause) {
		super(cause);
	}
	
}
