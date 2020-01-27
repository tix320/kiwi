package com.github.tix320.kiwi.internal.reactive.observable;

public class UnhandledObservableException extends RuntimeException {

	public UnhandledObservableException(Throwable cause) {
		super("Unhandled error from observable", cause);
	}
}
