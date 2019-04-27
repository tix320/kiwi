package com.gitlab.tixtix320.kiwi.check.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.check.Try;
import com.gitlab.tixtix320.kiwi.function.*;

public final class Failure<T> implements Try<T> {

	private final Exception exception;

	public static <T> Failure<T> of(Exception exception) {
		return new Failure<>(exception);
	}

	private Failure(Exception exception) {
		this.exception = exception;
	}

	@Override
	public <X extends Exception> void rethrow(Function<Exception, ? extends X> exMapper) throws X {
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
	public <X extends Exception> Optional<T> getOrElseThrow(CheckedSupplier<? extends X> exSupplier) throws X {
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
	public <X extends Exception> Optional<T> getOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper) throws X {
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
		throw new RecoverException(exception);
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
