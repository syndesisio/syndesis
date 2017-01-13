package com.redhat.ipaas.api.v1.rest.exception;

public class IPaasServerException extends RuntimeException {

	private static final long serialVersionUID = 3476018743129184217L;

	public IPaasServerException() {
		super();
	}

	public IPaasServerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IPaasServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public IPaasServerException(String message) {
		super(message);
	}

	public IPaasServerException(Throwable cause) {
		super(cause);
	}

}
