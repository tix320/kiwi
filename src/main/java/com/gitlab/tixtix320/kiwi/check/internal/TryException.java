package com.gitlab.tixtix320.kiwi.check.internal;

/**
 * @author Tigran.Sargsyan on 19-Dec-18
 */
public class TryException extends RuntimeException {

	private static final long serialVersionUID = -2990942621623055959L;

	public TryException(String message) {
		super(message);
	}

	public TryException(String message, Throwable cause) {
		super(message, cause);
	}
}
