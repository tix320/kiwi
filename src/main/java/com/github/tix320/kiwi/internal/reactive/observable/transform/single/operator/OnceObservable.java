package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends TransformObservable<T> implements MonoObservable<T> {

	private final Observable<T> observable;

	public OnceObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				subscriber.onPublish(item);
				return false;
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
