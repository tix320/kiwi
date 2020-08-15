package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.util.ExceptionUtils;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		if (count < 0) {
			throw new IllegalArgumentException("Count must not be negative");
		}
		this.observable = observable;
		this.count = count;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		AtomicLong limit = new AtomicLong(count);
		observable.subscribe(new Subscriber<T>() {

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
				long remaining = limit.decrementAndGet();
				if (remaining > 0) {
					boolean needMore = subscriber.onPublish(item);

					if (!needMore) {
						unsubscribed = true;
					}
					return needMore;
				}
				else {
					if (remaining == 0) {
						try {
							boolean needMore = subscriber.onPublish(item);

							if (!needMore) {
								unsubscribed = true;
							}
						}
						catch (Throwable e) {
							ExceptionUtils.applyToUncaughtExceptionHandler(e);
						}
					}
					return false;
				}
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
	}
}
