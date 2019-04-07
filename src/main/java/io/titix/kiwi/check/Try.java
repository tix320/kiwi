package io.titix.kiwi.check;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import io.titix.kiwi.check.internal.RecoverException;
import io.titix.kiwi.check.internal.TryException;
import io.titix.kiwi.function.*;

public final class Try<T> {

	private static final Try<?> EMPTY = new Try<>(null, null);

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

	public static void runAndRethrow(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");
		try {
			runnable.run();
		}
		catch (Exception e) {
			throw new RecoverException(e);
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

	public static <T> T supplyAndGet(CheckedSupplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Value supplier cannot be null");
		try {
			return supplier.get();
		}
		catch (Exception e) {
			throw new RecoverException(e);
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

	public Try<T> peek(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");
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

	public Try<T> filter(CheckedPredicate<? super T> predicate) {
		Objects.requireNonNull(predicate, "Predicate cannot be null");
		if (isPresent()) {
			try {
				if (predicate.test(value)) {
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

	public <M> Try<M> map(CheckedFunction<? super T, ? extends M> mapper) {
		Objects.requireNonNull(mapper, "Mapper cannot be null");
		if (isPresent()) {
			M newValue;
			try {
				newValue = mapper.apply(value);
			}
			catch (Exception e) {
				return typedFailure(e);
			}
			return success(newValue);
		}
		@SuppressWarnings("unchecked") Try<M> typed = (Try<M>) this;
		return typed;
	}

	public Try<T> whatever(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");
		try {
			runnable.run();
			return this;
		}
		catch (Exception e) {
			return typedFailure(e);
		}
	}

	public <X extends Exception> T getOrElseThrow(CheckedSupplier<? extends X> exSupplier) throws X {
		Objects.requireNonNull(exSupplier, "Exception supplier cannot be null");
		if (isPresent()) {
			return value;
		}
		X exception;
		try {
			exception = exSupplier.get();
		}
		catch (Exception e) {
			throw new TryException("An error occurred in exception supplier", e);
		}
		throw exception;
	}

	public <X extends Exception> T getOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper) throws X {
		Objects.requireNonNull(exMapper, "Exception mapper cannot be null");
		if (isPresent()) {
			return value;
		}
		X newException;
		try {
			newException = exMapper.apply(exception);
		}
		catch (Exception e) {
			throw new TryException("An error occurred in exception supplier", e);
		}
		throw newException;
	}

	public Try<T> onFailure(CheckedConsumer<Exception> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");
		if (isFailure()) {
			try {
				consumer.accept(exception);
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in exception consumer", exception);
			}
		}
		return this;
	}

	public Try<T> onFailure(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");
		if (isFailure()) {
			try {
				runnable.run();
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in runnable", exception);
			}
		}
		return this;
	}

	public Try<T> onSuccess(CheckedConsumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");
		if (isSuccess()) {
			try {
				consumer.accept(value);
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in success consumer", exception);
			}
		}
		return this;
	}

	public Try<T> onSuccess(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");
		if (isSuccess()) {
			try {
				runnable.run();
			}
			catch (Exception exception) {
				throw new TryException("An error occurred in success runnable", exception);
			}
		}
		return this;
	}

	public T get() {
		if (isFailure()) {
			throw new RecoverException(exception);
		}
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
		return value;
	}

	public boolean isEmpty() {
		return value == null && exception == null;
	}

	public boolean isPresent() {
		return value != null;
	}

	public boolean isUseless() {
		return value == null;
	}

	public boolean isSuccess() {
		return exception == null;
	}

	public boolean isFailure() {
		return exception != null;
	}

	@SuppressWarnings("unchecked")
	private static <M> Try<M> typedFailure(Exception exception) {
		return (Try<M>) failure(exception);
	}
}
