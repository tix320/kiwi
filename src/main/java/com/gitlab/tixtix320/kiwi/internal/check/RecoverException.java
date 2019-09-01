package com.gitlab.tixtix320.kiwi.internal.check;

public class RecoverException {

	private RecoverException() {

	}

	public static RuntimeException of(Exception exception) {
		return new RuntimeException("See cause", exception);
	}
}
