package io.titix.check.internal;

/**
 * @author Tigran.Sargsyan on 19-Dec-18
 */
public class TryException extends RuntimeException {

	public TryException(String message) {
		super(message);
	}

	public TryException(String message, Throwable cause) {
		super(message, cause);
	}
}
