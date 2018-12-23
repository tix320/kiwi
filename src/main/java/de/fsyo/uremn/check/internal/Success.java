package de.fsyo.uremn.check.internal;

import de.fsyo.uremn.check.Try;

public final class Success<T> implements Try<T> {

	public static final Success<?> EMPTY = new Success<>(null);

	private final T value;

	public Success(T value) {
		this.value = value;
	}

	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public boolean isFailure() {
		return false;
	}

	@Override
	public T get() {
		return value;
	}
}
