package com.gitlab.tixtix320.kiwi.check;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.check.internal.Failure;
import com.gitlab.tixtix320.kiwi.check.internal.RecoverException;
import com.gitlab.tixtix320.kiwi.check.internal.Success;
import com.gitlab.tixtix320.kiwi.function.*;

/**
 * This class provides work with checked exceptions in a functional style.
 *
 * @param <T> type of value, which is stored.
 */
public interface Try<T> {

	static <T> Try<T> success(T value) {
		return Success.of(value);
	}

	static <T> Try<T> success(CheckedSupplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Supplier can not be null");

		try {
			return Success.of(supplier.get());
		}
		catch (Exception e) {
			throw new IllegalStateException("An error occurred in supplier. See cause.", e);
		}
	}

	static Try<Void> failure(Exception exception) {
		Objects.requireNonNull(exception, "exception cannot be null");

		return Failure.of(exception);
	}

	static Try<Void> failure(CheckedSupplier<Exception> supplier) {
		Objects.requireNonNull(supplier, "Supplier can not be null");

		try {
			return Failure.of(supplier.get());
		}
		catch (Exception e) {
			throw new IllegalStateException("An error occurred in supplier. See cause.", e);
		}
	}

	static Try<Void> run(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");

		try {
			runnable.run();
			return Success.empty();
		}
		catch (Exception exception) {
			return Failure.of(exception);
		}
	}

	static <T> Try<T> supply(CheckedSupplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Supplier cannot be null");

		try {
			return Success.of(supplier.get());
		}
		catch (Exception e) {
			return Failure.of(e);
		}
	}

	static <T> T supplyAndGet(CheckedSupplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Supplier cannot be null");

		try {
			return supplier.get();
		}
		catch (Exception e) {
			throw RecoverException.of(e);
		}
	}

	static void runAndRethrow(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "Runnable cannot be null");

		try {
			runnable.run();
		}
		catch (Exception e) {
			throw RecoverException.of(e);
		}
	}

	static <T> T supplyAndRethrow(CheckedSupplier<T> supplier) {
		Objects.requireNonNull(supplier, "Supplier cannot be null");

		try {
			return supplier.get();
		}
		catch (Exception e) {
			throw RecoverException.of(e);
		}
	}

	<X extends Exception> void rethrow(Function<Exception, ? extends X> exMapper) throws X;

	<X extends Exception, M extends Exception> Try<Void> rethrow(Class<X> exceptionType,
																 Function<? super X, ? extends M> exMapper);

	<X extends Exception> Try<Void> rethrow(Class<X> exceptionType);

	Try<T> peek(CheckedConsumer<? super T> consumer);

	Try<T> peek(CheckedRunnable runnable);

	Try<T> filter(CheckedPredicate<? super T> predicate);

	<M> Try<M> map(CheckedFunction<? super T, ? extends M> mapper);

	Try<T> whatever(CheckedRunnable runnable);

	<X extends Exception, M> Try<M> recover(Class<X> exceptionType, Function<? super X, ? extends M> recoverFunction);

	<X extends Exception> Try<T> recover(Class<X> exceptionType, Consumer<? super X> recoverFunction);

	<X extends Exception> Optional<T> getOrElseThrow(CheckedSupplier<? extends X> exSupplier) throws X;

	<X extends Exception> Optional<T> getOrElseThrow(CheckedFunction<Exception, ? extends X> exMapper) throws X;

	Try<T> onFailure(CheckedConsumer<Exception> consumer);

	Try<T> onFailure(CheckedRunnable runnable);

	Try<T> onSuccess(CheckedConsumer<? super T> consumer);

	Try<T> onSuccess(CheckedRunnable runnable);

	Optional<T> get();

	boolean isEmpty();

	boolean isPresent();

	boolean isUseless();

	boolean isSuccess();

	boolean isFailure();
}
