package com.github.tix320.kiwi.internal.reactive.observable.transform.single.timeout;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.tix320.kiwi.api.reactive.observable.*;
import com.github.tix320.kiwi.api.util.Threads;

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

		SCHEDULER.schedule(() -> {
			try {
				if (published.compareAndSet(false, true)) {
					subscriber.onPublish(newItemFactory.get());
				}
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}, timeout.toMillis(), TimeUnit.MILLISECONDS);

		observable.toMono().subscribe(new Subscriber<T>() {

			private volatile boolean completedFromSubscriber = false;

			@Override
			public boolean onSubscribe(Subscription subscription) {
				return subscriber.onSubscribe(new Subscription() {
					@Override
					public boolean isCompleted() {
						return subscription.isCompleted();
					}

					@Override
					public void unsubscribe() {
						completedFromSubscriber = true;
						subscription.unsubscribe();
					}
				});
			}

			@Override
			public boolean onPublish(T item) {
				if (published.compareAndSet(false, true)) {
					subscriber.onPublish(item);
				}
				return false;
			}

			@Override
			public boolean onError(Throwable throwable) {
				subscriber.onError(throwable);
				return true;
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (completedFromSubscriber) {
					subscriber.onComplete(CompletionType.UNSUBSCRIPTION);
				}
				else {
					subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
				}
			}
		});
	}
}
