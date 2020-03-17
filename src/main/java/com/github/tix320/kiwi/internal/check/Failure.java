package com.github.tix320.kiwi.internal.check;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.function.*;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.api.util.WrapperException;

public final class Failure<T> implements Try<T> {

	private final Exception exception;

	public static <T> Failure<T> of(Exception exception) {
		return new Failure<>(exception);
	}

	private Failure(Exception exception) {
		this.exception = exception;
	}

	@Override
	public <X extends Exception> void rethrow(Function<Exception, ? extends X> exMapper)
			throws X {
		Objects.requireNonNull(exMapper, "Mapper cannot be null");

		X mappedException;
		try {
			mappedException = exMapper.apply(exception);
		}
		catch (Exception e) {
			throw new IllegalStateException("An error occurred in mapper. See cause.", e);
		}
		throw mappedException;
	}

	@Override
	public <X extends Exception, M extends Exception> Try<None> rethrowWhen(Class<X> expectedExceptionType,
																			Function<? super X, ? extends M> exMapper) {
		if (expectedExceptionType.isInstance(exception)) {
			try {
				@SuppressWarnings("unchecked")
				X exception = (X) this.exception;
				throw exMapper.apply(exception);
			}
			catch (Exception e) {
				throw new IllegalStateException("An error occurred in rethrow function. See cause.", e);
			}
		}

		@SuppressWarnings("unchecked")
		Try<None> typedThis = (Try<None>) this;
		return typedThis;
	}

	@Override
	public <X extends Exception> Try<None> rethrowWhen(Class<X> expectedExceptionType) {
		if (expectedExceptionType.isInstance(exception)) {
			try {
				throw WrapperException.wrap(exception);
			}
			catch (Exception e) {
				throw new IllegalStateException("An error occurred in rethrow function. See cause.", e);
			}
		}

		@SuppressWarnings("unchecked")
		Try<None> typedThis = (Try<None>) this;
		return typedThis;
	}

	@Override
	public Try<T> peek(CheckedConsumer<? super T> consumer) {
		return onSuccess(consumer);
	}

	@Override
	public Try<T> peek(CheckedRunnable runnable) {
		return onSuccess(runnable);
	}

	@Override
	public Try<T> filter(CheckedPredicate<? super T> predicate) {
		Objects.requireNonNull(predicate, "Predicate cannot be null");

		return this;
	}

	@Override
	public <M> Try<M> map(CheckedFunction<? super T, ? extends M> mapper) {
		Objects.requireNonNull(mapper, "Mapper cannot be null");

		@SuppressWarnings("unchecked")
		Try<M> typedThis = (Try<M>) this;
		return typedThis;
	}

	@Override
	public Try<T> whatever(CheckedRunnable runnable) {
		return onFailure(runnable);
	}

	@Override
	public <X extends Exception, M> Try<M> recover(Class<X> exceptionType,
												   Function<? super X, ? extends M> recoverFunction) {
		if (exceptionType.isInstance(exception)) {
			try {
				@SuppressWarnings("unchecked")
				X exception = (X) this.exception;
				return Success.of(recoverFunction.apply(exception));
			}
			catch (Exception e) {
				throw new IllegalStateException("An error occurred in recover function. See cause.", e);
			}
		}

		@SuppressWarnings("unchecked")
		Try<M> typedThis = (Try<M>) this;
		return typedThis;
	}

	@Override
	public <X extends Exception> Try<T> recover(Class<X> exceptionType, Consumer<? super X> recoverFunction) {
		if (exceptionType.isInstance(exception)) {
			try {
				@SuppressWarnings("unchecked")
				X exception = (X) this.exception;
				recoverFunction.accept(exception);
				return Success.empty();
			}
			catch (Exception e) {
				throw new IllegalStateException("An error occurred in recover function. See cause.", e);
			}
		}
		return this;
	}

	@Override
	public <X extends Exception> Optional<T> optionalOrElseThrow(CheckedSupplier<? extends X> exSupplier)
			throws X {
		Objects.requireNonNull(exSupplier, "Supplier cannot be null");

		X ex;
		try {
			ex = exSupplier.get();
		}
		catch (Exception e) {
			throw new IllegalStateException("An error occurred in supplier", e);
		}
		throw ex;
	}

	@Override
	public <X extends Exception> Optional<T> optionalOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper)
			throws X {
		Objects.requireNonNull(exMapper, "Mapper cannot be null");

		X newException;
		try {
			newException = exMapper.apply(exception);
		}
		catch (Exception e) {
			throw new IllegalStateException("An error occurred in mapper", e);
		}
		throw newException;
	}

	@Override
	public T getOrElse(T value) {
		return value;
	}

	@Override
	public T getOrElse(Supplier<T> valueSupplier) {
		Objects.requireNonNull(valueSupplier, "Supplier cannot be null");
		return valueSupplier.get();
	}

	@Override
	public <X extends Exception> T getOrElseThrow(CheckedSupplier<? extends X> exSupplier)
			throws X {
		optionalOrElseThrow(exSupplier);
		throw new IllegalStateException("Unreachable Code");
	}

	@Override
	public <X extends Exception> T getOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper)
			throws X {
		optionalOrElseThrow(exMapper);
		throw new IllegalStateException("Unreachable Code");
	}

	@Override
	public Try<T> onFailure(CheckedConsumer<Exception> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");

		try {
			consumer.accept(exception);
			return this;
		}
		catch (Exception e) {
			exception.addSuppressed(e);
			return Failure.of(exception);
		}
	}

	@Override
	public Try<T> onFailure(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");

		try {
			runnable.run();
			return this;
		}
		catch (Exception e) {
			exception.addSuppressed(e);
			return Failure.of(exception);
		}
	}

	@Override
	public Try<T> onSuccess(CheckedConsumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "Consumer cannot be null");

		return this;
	}

	@Override
	public Try<T> onSuccess(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");

		return this;
	}

	@Override
	public Optional<T> get() {
		throw WrapperException.wrap(exception);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isPresent() {
		return false;
	}

	@Override
	public boolean isUseless() {
		return true;
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public boolean isFailure() {
		return true;
	}
}
