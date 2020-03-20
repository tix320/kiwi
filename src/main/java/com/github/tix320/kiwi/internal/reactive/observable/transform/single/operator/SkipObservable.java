package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public class SkipObservable<T> extends TransformObservable<T> {

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
	public Subscription subscribe(Subscriber<? super T> subscriber) {
		AtomicLong mustSkip = new AtomicLong(count);
		return observable.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				if (mustSkip.get() == 0) {
					return subscriber.onPublish(item);
				}
				else if (mustSkip.get() > 0) {
					mustSkip.decrementAndGet();
					return true;
				}
				else {
					throw new IllegalStateException();
				}
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete() {
				subscriber.onComplete();
			}
		});
	}
}
