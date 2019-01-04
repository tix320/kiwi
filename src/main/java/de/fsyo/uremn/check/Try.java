package de.fsyo.uremn.check;

import de.fsyo.uremn.check.exception.internal.TryException;
import de.fsyo.uremn.check.internal.Failure;
import de.fsyo.uremn.check.internal.Success;

public interface Try<T> {

	@SuppressWarnings("unchecked")
	private static <T> Try<T> empty() {
		return (Try<T>) Success.EMPTY;
	}

	static <T> Try<T> success(T value) {
		if (value == null) {
			return empty();
		}
		return new Success<>(value);
	}

	static <T> Try<T> success(CheckedSupplier<? extends T> supplier) {
		T value;
		try {
			value = supplier.get();
		}
		catch (Throwable throwable) {
			throw new TryException("An error occurred in success supplier", throwable);
		}
		return success(value);
	}

	static Try<?> failure(Throwable throwable) {
		if (throwable == null) {
			throw new TryException("throwable cannot be null");
		}
		return new Failure(throwable);
	}

	static Try<?> failure(CheckedSupplier<Throwable> supplier) {
		Throwable throwable;
		try {
			throwable = supplier.get();
		}
		catch (Throwable e) {
			throw new TryException("An error occurred in failure supplier", e);
		}
		return failure(throwable);
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

	static <T> Try<T> supply(CheckedSupplier<? extends T> supplier) {
		T value;
		try {
			value = supplier.get();
		}
		catch (Throwable e) {
			return typedFailure(e);
		}
		return success(value);
	}

	default Try<T> peek(CheckedConsumer<? super T> consumer) {
		if (isPresent()) {
			try {
				consumer.accept(get());
				return this;
			}
			catch (Throwable e) {
				return typedFailure(e);
			}
		}
		return this;
	}

	default Try<T> peek(CheckedRunnable runnable) {
		if (isSuccess()) {
			try {
				runnable.run();
				return this;
			}
			catch (Throwable e) {
				return typedFailure(e);
			}
		}
		return this;
	}

	default Try<T> filter(CheckedPredicate<? super T> predicate) {
		if (isPresent()) {
			try {
				if (predicate.test(get())) {
					return this;
				}
				else {
					return empty();
				}
			}
			catch (Throwable e) {
				return typedFailure(e);
			}
		}
		return this;
	}

	default <M> Try<M> map(CheckedFunction<? super T, ? extends M> mapper) {
		if (isPresent()) {
			M value;
			try {
				value = mapper.apply(get());
			}
			catch (Throwable e) {
				return typedFailure(e);
			}
			return success(value);
		}
		return current();
	}

	default <X extends Throwable> Try<T> throwWhenFailed(CheckedFunction<Throwable, ? extends X> exSupplier) throws X {
		if (isFailure()) {
			X x;
			try {
				x = exSupplier.apply(getCause());
			}
			catch (Throwable throwable) {
				throw new TryException("An error occurred in failure supplier", throwable);
			}
			throw x;
		}
		else {
			return this;
		}
	}

	default <X extends Throwable> Try<T> throwWhenFailed(CheckedSupplier<? extends X> exSupplier) throws X {
		return throwWhenFailed(throwable -> exSupplier.get());
	}

	default <X extends Throwable> T getOrElseThrow(CheckedSupplier<? extends X> exSupplier) throws X {
		if (isPresent()) {
			return get();
		}
		X x;
		try {
			x = exSupplier.get();
		}
		catch (Throwable throwable) {
			throw new TryException("An error occurred in failure supplier", throwable);
		}
		throw x;
	}

	default <X extends Throwable> T getOrElseThrow(CheckedFunction<Throwable, ? extends X> exMapper) throws X {
		return getOrElseThrow(() -> exMapper.apply(getCause()));
	}

	default Try<T> onFailure(CheckedConsumer<Throwable> consumer) {
		if (isFailure()) {
			try {
				consumer.accept(getCause());
			}
			catch (Throwable throwable) {
				throw new TryException("An error occurred in failure consumer", throwable);
			}
		}
		return this;
	}

	default Try<T> onFailure(CheckedRunnable runnable) {
		return onFailure(throwable -> runnable.run());
	}

	default Try<T> onSuccess(CheckedConsumer<? super T> consumer) {
		if (isSuccess()) {
			try {
				consumer.accept(get());
			}
			catch (Throwable throwable) {
				throw new TryException("An error occurred in success consumer", throwable);
			}
		}
		return this;
	}

	default Try<T> onSuccess(CheckedRunnable runnable) {
		return onSuccess(value -> runnable.run());
	}

	default boolean isPresent() {
		return isSuccess() && !isEmpty();
	}

	default boolean isUseless() {
		return isFailure() || isEmpty();
	}

	boolean isSuccess();

	boolean isFailure();

	T get();

	@SuppressWarnings("unchecked")
	private <M> Try<M> current() {
		return (Try<M>) this;
	}

	private boolean isEmpty() {
		return this == Success.EMPTY;
	}

	private Throwable getCause() {
		return ((Failure) (this)).getCause();
	}

	@SuppressWarnings("unchecked")
	private static <M> Try<M> typedFailure(Throwable throwable) {
		return (Try<M>) failure(throwable);
	}
}
