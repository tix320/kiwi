package com.gitlab.tixtix320.kiwi.check.internal;

public class RecoverException {

	public static RuntimeException of(Exception exception) {
		return new RuntimeException("See cause", exception);
	}
}
