package de.fsyo.uremn.check.internal;

import java.util.NoSuchElementException;

import de.fsyo.uremn.check.Try;

public final class Failure implements Try<Throwable> {

	private final Throwable cause;

	public Failure(Throwable cause) {
		this.cause = cause;
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public boolean isFailure() {
		return true;
	}

	@Override
	public Throwable get() {
		throw new NoSuchElementException();
	}

	public Throwable getCause() {
		return cause;
	}
}
