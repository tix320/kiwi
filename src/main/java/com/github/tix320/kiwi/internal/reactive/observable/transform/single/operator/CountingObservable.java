package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> extends TransformObservable<T> {

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
	public Subscription subscribe(Subscriber<? super T> subscriber) {
		if (count == 0) {
			return () -> {
			};
		}
		AtomicLong limit = new AtomicLong(count);
		return observable.subscribe(new Subscriber<T>() {
			@Override
			public boolean consume(T item) {
				long remaining = limit.decrementAndGet();
				if (remaining > 0) {
					return subscriber.consume(item);
				}
				else if (remaining == 0) {
					subscriber.consume(item);
					return false;
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
