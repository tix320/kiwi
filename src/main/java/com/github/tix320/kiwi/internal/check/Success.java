package com.github.tix320.kiwi.internal.check;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.function.*;
import com.github.tix320.kiwi.api.util.None;

public class Success<T> implements Try<T> {

	private static final Success<?> EMPTY = new Success<>(null);

	private final T value;

	@SuppressWarnings("unchecked")
	public static <T> Success<T> empty() {
		return (Success<T>) EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static <T> Success<T> of(T value) {
		if (value == null) {
			return (Success<T>) EMPTY;
		}
		return new Success<>(value);
	}

	private Success(T value) {
		this.value = value;
	}

	@Override
	public <X extends Exception> void rethrow(Function<Exception, ? extends X> exMapper) {
		Objects.requireNonNull(exMapper, "Mapper cannot be null");
	}

	@Override
	public <X extends Exception, M extends Exception> Try<None> rethrowWhen(Class<X> expectedExceptionType,
																			Function<? super X, ? extends M> exMapper) {
		@SuppressWarnings("unchecked")
		Try<None> typedThis = (Try<None>) this;
		return typedThis;
	}

	@Override
	public <X extends Exception> Try<None> rethrowWhen(Class<X> expectedExceptionType) {
		@SuppressWarnings("unchecked")
		Try<None> typedThis = (Try<None>) this;
		return typedThis;
	}

	@Override
	public Try<T> peek(CheckedConsumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");

		if (isPresent()) {
			try {
				consumer.accept(value);
				return this;
			}
			catch (Exception e) {
				return Failure.of(e);
			}
		}
		return this;
	}

	@Override
	public Try<T> peek(CheckedRunnable runnable) {
		return onSuccess(runnable);
	}

	@Override
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
				return Failure.of(e);
			}
		}
		return this;
	}

	@Override
	public <M> Try<M> map(CheckedFunction<? super T, ? extends M> mapper) {
		Objects.requireNonNull(mapper, "Mapper cannot be null");

		if (isPresent()) {
			M newValue;
			try {
				newValue = mapper.apply(value);
			}
			catch (Exception e) {
				return Failure.of(e);
			}
			return Success.of(newValue);
		}

		@SuppressWarnings("unchecked")
		Try<M> typedThis = (Try<M>) this;
		return typedThis;
	}

	@Override
	public Try<T> whatever(CheckedRunnable runnable) {
		return onSuccess(runnable);
	}

	@Override
	public <X extends Exception, M> Try<M> recover(Class<X> exceptionType,
												   Function<? super X, ? extends M> recoverFunction) {
		@SuppressWarnings("unchecked")
		Try<M> typedThis = (Try<M>) this;
		return typedThis;
	}

	@Override
	public <X extends Exception> Try<T> recover(Class<X> exceptionType, Consumer<? super X> recoverFunction) {
		return this;
	}

	@Override
	public <X extends Exception> Optional<T> optionalOrElseThrow(CheckedSupplier<? extends X> exSupplier) {
		Objects.requireNonNull(exSupplier, "Supplier cannot be null");

		return Optional.ofNullable(value);
	}

	@Override
	public <X extends Exception> Optional<T> optionalOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper) {
		Objects.requireNonNull(exMapper, "Mapper cannot be null");

		return Optional.ofNullable(value);
	}

	@Override
	public <X extends Exception> T getOrElseThrow(CheckedSupplier<? extends X> exSupplier)
			throws X {
		Objects.requireNonNull(exSupplier, "Supplier cannot be null");

		return value;
	}

	@Override
	public <X extends Exception> T getOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper)
			throws X {
		Objects.requireNonNull(exMapper, "Mapper cannot be null");

		return value;
	}

	@Override
	public Try<T> onFailure(CheckedConsumer<Exception> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");

		return this;
	}

	@Override
	public Try<T> onFailure(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");

		return this;
	}

	@Override
	public Try<T> onSuccess(CheckedConsumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");

		try {
			consumer.accept(value);
			return this;
		}
		catch (Exception e) {
			return Failure.of(e);
		}
	}

	@Override
	public Try<T> onSuccess(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");
		try {
			runnable.run();
			return this;
		}
		catch (Exception e) {
			return Failure.of(e);
		}
	}

	@Override
	public Optional<T> get() {
		return Optional.ofNullable(value);
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}

	@Override
	public boolean isPresent() {
		return value != null;
	}

	@Override
	public boolean isUseless() {
		return value == null;
	}

	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public boolean isFailure() {
		return false;
	}
}
