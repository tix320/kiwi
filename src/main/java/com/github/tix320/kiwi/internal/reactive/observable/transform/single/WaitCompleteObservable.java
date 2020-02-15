package com.github.tix320.kiwi.internal.reactive.observable.transform.single;

import java.util.concurrent.CountDownLatch;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public final class WaitCompleteObservable<T> extends TransformObservable<T> {

	private final Observable<T> observable;

	public WaitCompleteObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public Subscription subscribe(Subscriber<? super T> subscriber) {
		CountDownLatch latch = new CountDownLatch(1);

		observable.subscribe(new Subscriber<T>() {
			@Override
			public boolean consume(T item) {
				boolean needMore = subscriber.consume(item);
				if (!needMore) {
					latch.countDown();
				}
				return needMore;
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete() {
				latch.countDown();
				subscriber.onComplete();
			}
		});

		Try.run(latch::await);

		return () -> {
		};
	}
}
