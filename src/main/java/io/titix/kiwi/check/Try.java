package io.titix.kiwi.check;

import java.util.Objects;
import java.util.function.Function;

import io.titix.kiwi.check.internal.TryException;

public final class Try<T> {

	public static final Try<?> EMPTY = new Try<>(null, null);

	private final T value;

	private final Exception exception;

	private Try(T value, Exception exception) {
		this.value = value;
		this.exception = exception;
	}

	@SuppressWarnings("unchecked")
	public static <T> Try<T> empty() {
		return (Try<T>) EMPTY;
	}

	public static <T> Try<T> success(T value) {
		if (value == null) {
			return empty();
		}
		return new Try<>(value, null);
	}

	public static <T> Try<T> success(CheckedSupplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Value supplier can not be null");
		try {
			return success(supplier.get());
		}
		catch (Exception exception) {
			throw new TryException("An error occurred in success supplier", exception);
		}
	}

	public static Try<?> failure(Exception exception) {
		Objects.requireNonNull(exception, "exception cannot be null");
		return new Try<>(null, exception);
	}

	public static Try<?> failure(CheckedSupplier<Exception> supplier) {
		Objects.requireNonNull(supplier, "Exception supplier can not be null");
		try {
			return failure(supplier.get());
		}
		catch (Exception e) {
			throw new TryException("An error occurred in failure supplier", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <M> Try<M> typedFailure(Exception exception) {
		return (Try<M>) failure(exception);
	}

	public static Try<?> run(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");
		try {
			runnable.run();
			return empty();
		}
		catch (Exception exception) {
			return failure(exception);
		}
	}

	public static <T> Try<T> supply(CheckedSupplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Value supplier cannot be null");
		try {
			return success(supplier.get());
		}
		catch (Exception e) {
			return typedFailure(e);
		}
	}

	public <X extends Exception> void rethrow(Function<Exception, ? extends X> exMapper) throws X {
		Objects.requireNonNull(exMapper, "Exception mapper cannot be null");
		if (isFailure()) {
			X mappedException;
			try {
				mappedException = exMapper.apply(exception);
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in exception mapper", exception);
			}
			throw mappedException;
		}
	}

	public Try<T> peek(CheckedConsumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");
		if (isPresent()) {
			try {
				consumer.accept(value);
			}
			catch (Exception e) {
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
			catch (Exception e) {
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
			catch (Exception e) {
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
			catch (Exception e) {
				return typedFailure(e);
			}
			return success(value);
		}
		return current();
	}

	default Try<T> whatever(CheckedRunnable runnable) {
		try {
			runnable.run();
			return this;
		}
		catch (Exception e) {
			return typedFailure(e);
		}
	}

	default <X extends Exception> Try<T> throwWhenFailed(CheckedFunction<Exception, ? extends X> exSupplier) throws X {
		if (isFailure()) {
			X x;
			try {
				x = exSupplier.apply(getException());
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in failure supplier", exception);
			}
			throw x;
		}
		else {
			return this;
		}
	}

	default <X extends Exception> Try<T> throwWhenFailed(CheckedSupplier<? extends X> exSupplier) throws X {
		return throwWhenFailed(exception -> exSupplier.get());
	}

	default <X extends Exception> T getOrElseThrow(CheckedSupplier<? extends X> exSupplier) throws X {
		if (isPresent()) {
			return get();
		}
		X x;
		try {
			x = exSupplier.get();
		}
		catch (Exception exception) {
			throw new TryException("An error occurred in failure supplier", exception);
		}
		throw x;
	}

	default <X extends Exception> T getOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper) throws X {
		return getOrElseThrow(() -> exMapper.apply(getException()));
	}

	default Try<T> onFailure(CheckedConsumer<Exception> consumer) {
		if (isFailure()) {
			try {
				consumer.accept(getException());
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in failure consumer", exception);
			}
		}
		return this;
	}

	default Try<T> onFailure(CheckedRunnable runnable) {
		return onFailure(exception -> runnable.run());
	}

	default Try<T> onSuccess(CheckedConsumer<? super T> consumer) {
		if (isSuccess()) {
			try {
				consumer.accept(get());
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in success consumer", exception);
			}
		}
		return this;
	}

	default Try<T> onSuccess(CheckedRunnable runnable) {
		return onSuccess(value -> runnable.run());
	}

	public boolean isEmpty() {
		return value == null && exception == null;
	}

	public boolean isPresent() {
		return value != null;
	}

	public boolean isUseless() {
		return value == null || exception != null;
	}

	public boolean isSuccess() {
		return exception == null;
	}

	public boolean isFailure() {
		return exception != null;
	}

	@SuppressWarnings("unchecked")
	private <M> Try<M> current() {
		return (Try<M>) this;
	}
}
