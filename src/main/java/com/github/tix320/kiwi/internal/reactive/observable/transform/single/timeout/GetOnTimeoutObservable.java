package com.github.tix320.kiwi.internal.reactive.observable.transform.single.timeout;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.tix320.kiwi.api.reactive.observable.*;
import com.github.tix320.kiwi.api.util.ExceptionUtils;
import com.github.tix320.kiwi.api.util.Threads;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 08-Apr-20.
 */
public class GetOnTimeoutObservable<T> implements MonoObservable<T> {

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

		observable.toMono().subscribe(new Subscriber<T>() {

			private volatile boolean unsubscribed = false;

			@Override
			public boolean onSubscribe(Subscription subscription) {
				return subscriber.onSubscribe(new Subscription() {
					@Override
					public boolean isCompleted() {
						return subscription.isCompleted();
					}

					@Override
					public void unsubscribe() {
						unsubscribed = true;
						subscription.unsubscribe();
					}
				});
			}

			@Override
			public boolean onPublish(T item) {
				if (published.compareAndSet(false, true)) {
					boolean needMore = subscriber.onPublish(item);
					if (!needMore) {
						unsubscribed = true;
					}
				}
				return false;
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (unsubscribed) {
					subscriber.onComplete(CompletionType.UNSUBSCRIPTION);
				}
				else {
					subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
				}
			}
		});

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		SCHEDULER.schedule(() -> {
			try {
				if (published.compareAndSet(false, true)) {
					BasePublisher.runAsync(() -> {
						try {
							subscriber.onPublish(newItemFactory.get());
							subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
						}
						catch (Throwable e) {
							ExceptionUtils.appendAsyncStacktrace(stackTrace, e);
							throw e;
						}
					});
				}
			}
			catch (Throwable t) {
				ExceptionUtils.applyToUncaughtExceptionHandler(t);
			}
		}, timeout.toMillis(), TimeUnit.MILLISECONDS);
	}
}
