package com.github.tix320.kiwi.observable.transform.single.timeout.internal;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.skimp.api.exception.ExceptionUtils;
import com.github.tix320.skimp.api.thread.Threads;

/**
 * @author Tigran Sargsyan on 08-Apr-20.
 */
public class GetOnTimeoutObservable<T> extends MonoObservable<T> {

	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(
			Threads::daemon);

	private static final SourceCompletion SOURCE_COMPLETED_BY_TIMEOUT = new SourceCompletion(
			"SOURCE_COMPLETED_BY_TIMEOUT");

	private final Observable<? extends T> observable;

	private final Duration timeout;

	private final Supplier<? extends T> newItemFactory;

	public GetOnTimeoutObservable(Observable<? extends T> observable, Duration timeout,
								  Supplier<? extends T> newItemFactory) {
		if (timeout.isNegative()) {
			throw new IllegalArgumentException(timeout.toString());
		}

		this.observable = observable;
		this.timeout = timeout;
		this.newItemFactory = newItemFactory;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		AtomicBoolean published = new AtomicBoolean(false);

		Object lock = new Object();

		final AbstractSubscriber<T> newSubscriber = new AbstractSubscriber<>() {

			@Override
			public void onSubscribe() {
				subscriber.onSubscribe(subscription());
			}

			@Override
			public void onPublish(T item) {
				synchronized (lock) {
					if (published.compareAndSet(false, true)) {
						subscriber.onPublish(item);
					}
				}
			}

			@Override
			public void onComplete(Completion completion) {
				synchronized (lock) {
					if (completion instanceof TimeoutUnsubscription timeoutUnsubscription) {
						T item = timeoutUnsubscription.data();

						try {
							subscriber.onPublish(item);
						}
						catch (Throwable e) {
							ExceptionUtils.applyToUncaughtExceptionHandler(e);
						}

						subscriber.onComplete(SOURCE_COMPLETED_BY_TIMEOUT);
					}
					else {
						subscriber.onComplete(completion);
					}
				}
			}
		};

		observable.toMono().subscribe(newSubscriber);

		schedule(timeout.toMillis(), () -> BasePublisher.runAsync(() -> {
			synchronized (lock) {
				if (published.compareAndSet(false, true)) {
					T item = newItemFactory.get();
					newSubscriber.subscription().cancelImmediately(new TimeoutUnsubscription(item));
				}
			}

		}));
	}

	private void schedule(long millisDelay, Runnable runnable) {
		try {
			SCHEDULER.schedule(runnable, millisDelay, TimeUnit.MILLISECONDS);
		}
		catch (Throwable e) {
			ExceptionUtils.applyToUncaughtExceptionHandler(e);
		}
	}

	private static final class TimeoutUnsubscription extends Unsubscription {

		public TimeoutUnsubscription(Object data) {
			super(data);
		}
	}
}
