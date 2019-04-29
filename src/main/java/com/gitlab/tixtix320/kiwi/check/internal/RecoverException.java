package com.gitlab.tixtix320.kiwi.check.internal;

public class RecoverException extends RuntimeException {

	public RecoverException(Throwable cause) {
		super("See cause", cause);
	}
}
