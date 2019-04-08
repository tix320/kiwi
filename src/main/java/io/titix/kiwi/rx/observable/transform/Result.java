package io.titix.kiwi.rx.observable.transform;

import java.util.Optional;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class Result<T> {

	private static final Result<?> EMPTY = new Result<>(null);

	private final T object;

	private Result(T object) {
		this.object = object;
	}

	public Optional<T> get() {
		return Optional.ofNullable(object);
	}

	public static <T> Result<T> of(T object) {
		return new Result<>(object);
	}

	@SuppressWarnings("unchecked")
	public static <T> Result<T> none() {
		return (Result<T>) EMPTY;
	}
}
