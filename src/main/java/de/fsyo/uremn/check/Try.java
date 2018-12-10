package de.fsyo.uremn.check;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Try<T> {

	static <T> Try<T> success(T value) {
		if (value == null) {
			return empty();
		}
		return new Success<>(value);
	}

	static <T> Try<T> success(Supplier<? extends T> supplier) {
		return success(supplier.get());
	}

	static <T> Try<T> failure(Throwable throwable) {
		if (throwable == null) {
			throw new IllegalArgumentException("throwable cannot be null");
		}
		@SuppressWarnings("unchecked")
		Try<T> t = (Try<T>) new Failure<>(throwable);
		return t;
	}

	static Try<?> failure(Supplier<Throwable> supplier) {
		return failure(supplier.get());
	}

	static Try<?> run(Runnable runnable) {
		try {
			runnable.run();
			return empty();
		}
		catch (Throwable e) {
			return new Failure<>(e);
		}
	}

	default Try<T> filter(Predicate<? super T> predicate) {
		if (isFailure() || isEmpty()) {
			return this;
		}
		try {
			if (predicate.test(get())) {
				return success(get());
			}
			else {
				return empty();
			}
		}
		catch (Throwable e) {
			return failure(e);

		}

	}

	default <M> Try<M> map(Function<? super T, ? extends M> mapper) {
		if (isFailure() || isEmpty()) {
			return current();
		}
		try {
			return success(mapper.apply(get()));
		}
		catch (Throwable e) {
			return failure(e);
		}
	}

	default <X extends Throwable> T getOrElseThrow(Supplier<? extends X> exSupplier) throws X {
		if (isFailure() || isEmpty()) {
			throw exSupplier.get();
		}
		else {
			return get();
		}
	}

	default <X extends Throwable> T getOrElseThrow(Function<T, ? extends X> exMapper) throws X {
		return getOrElseThrow(() -> exMapper.apply(get()));
	}

	private boolean isEmpty() {
		return this == Success.EMPTY;
	}

	@SuppressWarnings("unchecked")
	private static <T> Try<T> empty() {
		return (Try<T>) Success.EMPTY;
	}

	@SuppressWarnings("unchecked")
	private <M> Try<M> current() {
		return (Try<M>) this;
	}

	boolean isSuccess();

	boolean isFailure();

	T get();

}
