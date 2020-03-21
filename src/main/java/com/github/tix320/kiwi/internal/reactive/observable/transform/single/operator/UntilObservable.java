package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

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
		return observable.subscribe(new Subscriber<>() {

			private volatile Subscription subscription;

			private volatile boolean completed;

			@Override
			public void onSubscribe(Subscription subscription) {
				this.subscription = subscription;
				subscriber.onSubscribe(subscription);
				until.subscribe(o -> {}, () -> {
					completed = true;
					subscription.unsubscribe();
				});
			}

			@Override
			public boolean onPublish(T item) {
				until.subscribe(o -> {}, () -> {
					completed = true;
					subscription.unsubscribe();
				});
				if (completed) {
					return false;
				}
				return subscriber.onPublish(item);
			}

			@Override
			public boolean onError(Throwable throwable) {
				until.subscribe(o -> {}, () -> {
					completed = true;
					subscription.unsubscribe();
				});
				if (completed) {
					return false;
				}
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete() {
				subscriber.onComplete();
			}
		});
	}
}
