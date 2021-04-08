package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.observable.CompletionType;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;

public class SkipObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final long count;

	public SkipObservable(Observable<T> observable, long count) {
		if (count < 0) {
			throw new IllegalArgumentException("Count must not be negative");
		}
		this.observable = observable;
		this.count = count;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		AtomicLong mustSkip = new AtomicLong(count);
		observable.subscribe(new Subscriber<T>() {
			@Override
			public boolean onSubscribe(Subscription subscription) {
				return subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				long remaining = mustSkip.decrementAndGet();
				if (remaining >= 0) {
					return true;
				}
				else {
					return subscriber.onPublish(item);
				}
			}

			@Override
			public void onComplete(CompletionType completionType) {
				subscriber.onComplete(completionType);
			}
		});
	}
}
