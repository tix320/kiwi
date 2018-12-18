package de.fsyo.uremn.check;

public interface Try<T> {

	static <T> Try<T> success(T value) {
		if (value == null) {
			return empty();
		}
		return new Success<>(value);
	}

	static <T> Try<T> success(CheckedSupplier<? extends T> supplier) {
		try {
			return success(supplier.get());
		}
		catch (Throwable throwable) {
			throw new IllegalStateException("An error occurred in success supplier", throwable);
		}
	}

	static <T> Try<T> failure(Throwable throwable) {
		if (throwable == null) {
			throw new IllegalArgumentException("throwable cannot be null");
		}
		@SuppressWarnings("unchecked")
		Try<T> t = (Try<T>) new Failure<>(throwable);
		return t;
	}

	static Try<?> failure(CheckedSupplier<Throwable> supplier) {
		try {
			return failure(supplier.get());
		}
		catch (Throwable throwable) {
			throw new IllegalStateException("An error occurred in failure supplier", throwable);
		}
	}

	static Try<?> run(CheckedRunnable runnable) {
		try {
			runnable.run();
			return empty();
		}
		catch (Throwable e) {
			return failure(e);
		}
	}

	static <T> Try<T> supply(CheckedSupplier<T> supplier) {
		try {
			return success(supplier.get());
		}
		catch (Throwable e) {
			return failure(e);
		}
	}

	default Try<T> peek(CheckedConsumer<T> consumer) {
		if (isFailure() || isEmpty()) {
			return this;
		}
		try {
			consumer.accept(get());
			return this;
		}
		catch (Throwable throwable) {
			return failure(throwable);
		}
	}

	default Try<?> peek(CheckedRunnable runnable) {
		if (isFailure()) {
			return this;
		}
		try {
			runnable.run();
			return this;
		}
		catch (Throwable throwable) {
			return failure(throwable);
		}
	}

	default Try<T> filter(CheckedPredicate<? super T> predicate) {
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

	default <M> Try<M> map(CheckedFunction<? super T, ? extends M> mapper) {
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

	default <X extends Throwable> T getOrElseThrow(CheckedSupplier<? extends X> exSupplier) throws X {
		if (isFailure() || isEmpty()) {
			try {
				throw exSupplier.get();
			}
			catch (Throwable throwable) {
				throw new IllegalStateException("An error occurred in failure supplier", throwable);
			}
		}
		else {
			return get();
		}
	}

	default <X extends Throwable> T getOrElseThrow(CheckedFunction<T, ? extends X> exMapper) throws X {
		return getOrElseThrow(() -> exMapper.apply(get()));
	}

	default Try<T> onFailure(CheckedConsumer<Throwable> consumer) {
		if (isFailure()) {
			try {
				consumer.accept((Throwable) get());
			}
			catch (Throwable throwable) {
				throw new IllegalStateException("An error occurred in failure consumer", throwable);
			}
		}
		return this;
	}

	default Try<T> onFailure(CheckedRunnable runnable) {
		return onFailure(throwable -> runnable.run());
	}

	default Try<T> onSuccess(CheckedConsumer<T> consumer) {
		if (isSuccess()) {
			try {
				consumer.accept(get());
			}
			catch (Throwable throwable) {
				throw new IllegalStateException("An error occurred in success consumer", throwable);
			}
		}
		return this;
	}

	default Try<T> onSuccess(CheckedRunnable runnable) {
		return onSuccess(value -> runnable.run());
	}

	@SuppressWarnings("unchecked")
	private static <T> Try<T> empty() {
		return (Try<T>) Success.EMPTY;
	}

	@SuppressWarnings("unchecked")
	private <M> Try<M> current() {
		return (Try<M>) this;
	}

	default boolean isEmpty() {
		return this == Success.EMPTY;
	}

	default boolean isPresent() {
		return isSuccess() && !isEmpty();
	}

	boolean isSuccess();

	boolean isFailure();

	T get();

}
