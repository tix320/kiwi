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
public class GetOnTimeoutObservable<T> implements MonoObservable<T> {

	private static final RegularUnsubscription ALREADY_PUBLISHED = new RegularUnsubscription(null);

	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(
			Threads::daemon);

	private final Observable<? extends T> observable;

	private final Duration timeout;

	private final Supplier<? extends T> newItemFactory;

	public GetOnTimeoutObservable(Observable<? extends T> observable, Duration timeout,
								  Supplier<? extends T> newItemFactory) {
		this.observable = observable;
		this.timeout = timeout;
		this.newItemFactory = newItemFactory;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		AtomicBoolean published = new AtomicBoolean(false);

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		final Subscriber<T> newSubscriber = new Subscriber<>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public void onPublish(T item) {
				if (published.compareAndSet(false, true)) {

					try {
						RegularUnsubscription regularUnsubscription = subscriber.onPublish(item);
						if (regularUnsubscription != null) {
							return regularUnsubscription;
						}
					} catch (Throwable e) {
						if (BasePublisher.ENABLE_TRACER) {
							ExceptionUtils.appendStacktraceToThrowable(e, stackTrace);
						}

						ExceptionUtils.applyToUncaughtExceptionHandler(e);
					}

					return RegularUnsubscription.DEFAULT;
				} else {
					return ALREADY_PUBLISHED;
				}
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion != ALREADY_PUBLISHED) {
					subscriber.onComplete(completion);
				}
			}
		};

		observable.toMono().subscribe(newSubscriber);

		schedule(() -> BasePublisher.runAsync(() -> {
			final RegularUnsubscription regularUnsubscription = newSubscriber.onPublish(newItemFactory.get());
			if (regularUnsubscription != ALREADY_PUBLISHED) {
				subscriber.onComplete(SourceCompleted.DEFAULT);
			}
		}), timeout.toMillis());
	}

	private void schedule(Runnable runnable, long millisDelay) {
		try {
			SCHEDULER.schedule(runnable, millisDelay, TimeUnit.MILLISECONDS);
		} catch (Throwable e) {
			ExceptionUtils.applyToUncaughtExceptionHandler(e);
		}
	}
}
