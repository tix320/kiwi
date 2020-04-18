package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import com.github.tix320.kiwi.api.reactive.observable.*;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> implements MonoObservable<T> {

	private final Observable<T> observable;

	public OnceObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<T>() {

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
				subscriber.onPublish(item);
				return false;
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
