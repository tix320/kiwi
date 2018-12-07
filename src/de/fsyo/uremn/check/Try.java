package de.fsyo.uremn.check;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Try<T> {

	static <T> Try<T> success(T value) {
		return new Success<>(value);
	}

	static <T> Try<T> success(Supplier<T> supplier) {
		return new Success<>(supplier.get());
	}

	static Try<?> failure(Throwable value) {
		return new Failure<>(value);
	}

	static Try<?> failure(Supplier<Throwable> supplier) {
		return new Failure<>(supplier.get());
	}

	static Try<?> run(Runnable runnable) {
		try {
			runnable.run();
			return Success.EMPTY;
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
				return new Success<>(get());
			}
			else {
				@SuppressWarnings("unchecked")
				Try<T> t = (Try<T>) Success.EMPTY;
				return t;
			}
		}
		catch (Throwable e) {
			@SuppressWarnings("unchecked")
			Try<T> failure = (Try<T>) new Failure<>(e);
			return failure;
		}

	}

	default <M> Try<M> map(Function<? super T, ? extends M> mapper) {
		if (isFailure() || isEmpty()) {
			@SuppressWarnings("unchecked")
			Try<M> t = (Try<M>) this;
			return t;
		}
		try {
			return new Success<>(mapper.apply(get()));
		}
		catch (Throwable e) {
			@SuppressWarnings("unchecked")
			Try<M> failure = (Try<M>) new Failure<>(e);
			return failure;
		}
	}

	default <X extends Throwable> T getOrElseThrow(Supplier<? extends X> exSupplier) throws X {
		if (isFailure()) {
			throw exSupplier.get();
		}
		else {
			return get();
		}
	}

	default <X extends Throwable> T getOrElseThrow(Function<T, ? extends X> exMapper) throws X {
		if (isFailure()) {
			throw exMapper.apply(get());
		}
		if (isEmpty()) {
			@SuppressWarnings("unchecked")
			T noSuchElementException = (T) new NoSuchElementException();
			throw exMapper.apply(noSuchElementException);
		}
		return get();
	}

	private boolean isEmpty() {
		return this == Success.EMPTY;
	}

	boolean isSuccess();

	boolean isFailure();

	T get();

}
