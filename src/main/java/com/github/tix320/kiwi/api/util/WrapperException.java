package com.github.tix320.kiwi.api.util;

public class WrapperException extends RuntimeException {

	private WrapperException(String message, Throwable cause) {
		super(message, cause);
	}

	public static RuntimeException wrap(Throwable throwable) {
		return new WrapperException("See the real cause", throwable);
	}

	public static void wrapAndThrow(Throwable throwable) {
		throw wrap(throwable);
	}
}
