package com.github.tix320.kiwi.observable;

import com.github.tix320.skimp.exception.ThreadInterruptedException;
import java.time.Duration;
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
		});

		state.await(timeout);
	}

	static <T> T get(Observable<T> observable, Duration timeout) {
		AtomicReference<T> itemHolder = new AtomicReference<>();
		waitAndApply(observable, timeout, itemHolder::set);
		return itemHolder.get();
	}

	static <T> void waitAndApply(Observable<T> observable, Duration timeout, Consumer<T> consumer) {
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
		});

		state.await(timeout);
	}

	private static final class WaitState {

		private final CountDownLatch latch = new CountDownLatch(1);
		private final AtomicBoolean finished = new AtomicBoolean(false);
		private final AtomicReference<Subscription> subscription = new AtomicReference<>();

		void subscribe(Subscription value) {
			subscription.set(value);
			value.requestUnbounded();
		}

		void finish() {
			if (tryFinish()) {
				signal();
			}
		}

		boolean tryFinish() {
			return finished.compareAndSet(false, true);
		}

		void signal() {
			latch.countDown();
		}

		void await(Duration timeout) {
			long millis = timeout.toMillis();
			if (millis < 0) {
				awaitIndefinitely();
				return;
			}

			try {
				if (!latch.await(millis, TimeUnit.MILLISECONDS) && cancel()) {
					throw new TimeoutException(String.format("The observable not completed in %sms", millis));
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
			cancel();
			Thread.currentThread().interrupt();
			throw new ThreadInterruptedException();
		}

		private boolean cancel() {
			if (!tryFinish()) {
				return false;
			}

			Subscription value = subscription.get();
			if (value != null) {
				value.cancel();
			}
			return true;
		}

	}

}
