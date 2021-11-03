package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;

public final class SkipObservable<T> extends Observable<T> {

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
		observable.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(T item) {
				long remaining = mustSkip.decrementAndGet();
				if (remaining < 0) {
					subscriber.publish(item);
				}
			}

			@Override
			public void onComplete(Completion completion) {
				subscriber.complete(completion);
			}
		});
	}
}
