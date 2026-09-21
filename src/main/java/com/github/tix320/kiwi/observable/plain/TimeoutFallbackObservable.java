package com.github.tix320.kiwi.observable.plain;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.publisher.Publisher;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class TimeoutFallbackObservable<T> extends MonoObservable<T> {

	private final long createdNanos = System.nanoTime();
	private final long timeoutNanos;
	private final Supplier<T> factory;

	public TimeoutFallbackObservable(Duration timeout, Supplier<T> factory) {
		Objects.requireNonNull(timeout);
		if (timeout.isNegative()) {
			throw new IllegalArgumentException("Negative timeout: " + timeout);
		}
		this.timeoutNanos = toNanosSaturated(timeout);
		this.factory = Objects.requireNonNull(factory);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		var publisher = Publisher.<T>mono();
		AtomicBoolean cancelled = new AtomicBoolean(false);

		publisher.asObservable().subscribe(subscriber.fork(new MinorSubscriber<T, T>() {
			@Override
			protected void onSubscribe(Subscription sourceSubscription) {
				subscriber.setSubscription(new Subscription() {
					@Override
					protected void onRequest(long count) {
						sourceSubscription.request(count);
					}

					@Override
					protected void onUnboundRequest() {
						sourceSubscription.requestUnbounded();
					}

					@Override
					protected void onCancel(Unsubscription unsubscription) {
						cancelled.set(true);
						sourceSubscription.cancel(unsubscription);
					}
				});
			}

			@Override
			protected void onNext(T item) {
				subscriber.publish(item);
			}

			@Override
			protected void onComplete(Completion completion) {
				subscriber.complete(completion);
			}
		}));

		long elapsedNanos = System.nanoTime() - createdNanos;
		long remainingNanos = elapsedNanos >= timeoutNanos ? 0 : timeoutNanos - Math.max(0, elapsedNanos);
		KiwiSchedulerHolder.get().schedule(remainingNanos, TimeUnit.NANOSECONDS, () -> {
			if (cancelled.get()) {
				return;
			}

			T item;
			try {
				item = Objects.requireNonNull(factory.get(), "Timeout fallback value");
			} catch (Exception exception) {
				publisher.abort(exception);
				return;
			}

			publisher.publish(item);
		});
	}

	private static long toNanosSaturated(Duration timeout) {
		try {
			return timeout.toNanos();
		} catch (ArithmeticException ignored) {
			return Long.MAX_VALUE;
		}
	}

}
