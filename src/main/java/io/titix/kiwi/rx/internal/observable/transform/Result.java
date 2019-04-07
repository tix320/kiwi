package io.titix.kiwi.rx.internal.observable.transform;

import java.util.Optional;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
final class Result<T> {

	private static final Result<?> EMPTY = new Result<>(null);

	private final T object;

	private Result(T object) {
		this.object = object;
	}

	public Optional<T> get() {
		return Optional.ofNullable(object);
	}

	static <T> Result<T> of(T object) {
		return new Result<>(object);
	}

	@SuppressWarnings("unchecked")
	static <T> Result<T> empty() {
		return (Result<T>) EMPTY;
	}
}
