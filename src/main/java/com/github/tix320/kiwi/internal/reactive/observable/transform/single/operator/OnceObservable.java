package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import com.github.tix320.kiwi.api.reactive.observable.*;
import com.github.tix320.kiwi.internal.reactive.publisher.SubscriberException;

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
			public boolean onSubscribe(Subscription subscription) {
				return subscriber.onSubscribe(new Subscription() {
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
				try {
					subscriber.onPublish(item);
				}
				catch (Exception e) {
					new SubscriberException("An error while publishing to subscriber", e).printStackTrace();
				}

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
