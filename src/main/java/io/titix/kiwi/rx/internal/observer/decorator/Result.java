package io.titix.kiwi.rx.internal.observer.decorator;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
final class Result<T> {

	private static final Result<?> END = new Result<>(true, null);

	final boolean done;

	final T object;

	private Result(boolean done, T object) {
		this.done = done;
		this.object = object;
	}

	static <T> Result<T> forNext(T object) {
		return new Result<>(false, object);
	}

	@SuppressWarnings("unchecked")
	static <T> Result<T> end() {
		return (Result<T>) END;
	}
}
