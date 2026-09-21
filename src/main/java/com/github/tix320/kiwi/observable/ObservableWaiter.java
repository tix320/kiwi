package com.github.tix320.kiwi.observable;

import com.github.tix320.skimp.exception.ExceptionUtils;
import com.github.tix320.skimp.exception.ThreadInterruptedException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class ObservableWaiter {

	private ObservableWaiter() {
	}

	static <T> void await(Observable<T> observable, Duration timeout) {
		WaitState state = new WaitState();

		observable.subscribe(new FlexibleSubscriber<T>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				state.subscribe(subscription);
			}

			@Override
			public void onComplete(Completion completion) {
				state.finish();
			}

			@Override
			protected void onError(Throwable error) {
				state.fail(error);
			}
		});

		state.await(timeout);
		state.rethrowFailure();
	}

	static <T> T get(Observable<T> observable, Duration timeout) {
		AtomicReference<T> itemHolder = new AtomicReference<>();
		waitAndApply(observable, timeout, itemHolder::set);
		return itemHolder.get();
	}

	static <T> void waitAndApply(Observable<T> observable, Duration timeout, Consumer<T> consumer) {
		Objects.requireNonNull(consumer, "Consumer must not be null");
		WaitState state = new WaitState();

		observable.toMono().subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				state.subscribe(subscription);
			}

			@Override
			public void onNext(T item) {
				if (state.tryFinish()) {
					try {
						consumer.accept(item);
					} finally {
						state.signal();
					}
				}
			}

			@Override
			public void onComplete(Completion completion) {
				state.finish();
			}

			@Override
			protected void onError(Throwable error) {
				state.fail(error);
			}
		});

		state.await(timeout);
		state.rethrowFailure();
	}

	private static final class WaitState {

		private final CountDownLatch latch = new CountDownLatch(1);
		private final AtomicBoolean finished = new AtomicBoolean(false);
		private final AtomicReference<Subscription> subscription = new AtomicReference<>();
		private final AtomicReference<Throwable> failure = new AtomicReference<>();

		void subscribe(Subscription value) {
			subscription.set(value);
			value.requestUnbounded();
		}

		void finish() {
			if (tryFinish()) {
				signal();
			}
		}

		void fail(Throwable error) {
			if (tryFinish()) {
				failure.set(error);
				signal();
			}
		}

		void rethrowFailure() {
			Throwable error = failure.get();
			if (error != null) {
				ExceptionUtils.sneakyThrow(error);
			}
		}

		boolean tryFinish() {
			return finished.compareAndSet(false, true);
		}

		void signal() {
			latch.countDown();
		}

		void await(Duration timeout) {
			Objects.requireNonNull(timeout, "Timeout must not be null");
			if (timeout.isNegative()) {
				awaitIndefinitely();
				return;
			}
			long millis = timeout.toMillis();
			if (!timeout.minusMillis(millis).isZero() && millis < Long.MAX_VALUE) {
				millis++;
			}

			try {
				if (!latch.await(millis, TimeUnit.MILLISECONDS)) {
					if (tryFinish()) {
						Exception cancellationFailure = cancelSubscription();
						TimeoutException timeoutException =
							new TimeoutException(String.format("The observable not completed in %sms", millis));
						addSuppressed(timeoutException, cancellationFailure);
						throw timeoutException;
					}

					latch.await();
				}
			} catch (InterruptedException exception) {
				handleInterruption();
			}
		}

		private void awaitIndefinitely() {
			try {
				latch.await();
			} catch (InterruptedException exception) {
				handleInterruption();
			}
		}

		private void handleInterruption() {
			Thread.currentThread().interrupt();
			Exception cancellationFailure = tryFinish() ? cancelSubscription() : null;
			ThreadInterruptedException interruptedException = new ThreadInterruptedException();
			addSuppressed(interruptedException, cancellationFailure);
			throw interruptedException;
		}

		private Exception cancelSubscription() {
			Subscription value = subscription.get();
			if (value != null) {
				try {
					value.cancel();
				} catch (Exception exception) {
					return exception;
				}
			}
			return null;
		}

		private void addSuppressed(Exception exception, Exception suppressed) {
			if (suppressed != null) {
				exception.addSuppressed(suppressed);
			}
		}

	}

}
