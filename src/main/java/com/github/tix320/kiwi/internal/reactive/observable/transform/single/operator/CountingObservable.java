package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

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
		observable.subscribe(new Subscriber<>() {

			private volatile boolean completedFromSubscriber = false;

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(new Subscription() {
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
				long remaining = limit.decrementAndGet();
				if (remaining > 0) {
					return subscriber.onPublish(item);
				}
				else {
					if (remaining == 0) {
						subscriber.onPublish(item);
					}

					return false;
				}
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
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
