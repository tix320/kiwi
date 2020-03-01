package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends TransformObservable<T> {

	private final Observable<T> observable;

	private final Observable<?> until;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.until = until;
	}

	@Override
	public Subscription subscribe(Subscriber<? super T> subscriber) {
		AtomicBoolean completed = new AtomicBoolean(false);
		until.subscribe(o -> {}, () -> completed.set(true));

		return observable.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				if (completed.get()) {
					return false;
				}
				return subscriber.onPublish(item);
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
