package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.internal.SharedSubscriber;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends Observable<T> {

	private static final Unsubscription UNTIL_UNSUBSCRIPTION = new Unsubscription();

	private static final SourceCompleted SOURCE_COMPLETED_VIA_UNTIL = new SourceCompleted("SOURCE_COMPLETED_VIA_UNTIL");

	private final Observable<T> observable;

	private final Observable<?> until;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.until = until;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		Object lock = new Object();

		var context = new Object() {
			private volatile Subscription untilSubscription;
		};

		observable.subscribe(new SharedSubscriber<>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);

				until.subscribe(new SharedSubscriber<Object>() {
					@Override
					public void onSubscribe(Subscription subscription) {
						context.untilSubscription = subscription;
					}

					@Override
					public void onPublish(Object item) {

					}

					@Override
					public void onComplete(Completion completion) {
						if (completion instanceof SourceCompleted) {
							synchronized (lock) {
								subscription.cancelImmediately(UNTIL_UNSUBSCRIPTION);
							}
						}
					}
				});
			}

			@Override
			public void onPublish(T item) {
				synchronized (lock) {
					subscriber.onPublish(item);
				}
			}

			@Override
			public void onComplete(Completion completion) {
				synchronized (lock) {
					if (completion == UNTIL_UNSUBSCRIPTION) {
						subscriber.onComplete(SOURCE_COMPLETED_VIA_UNTIL);
					}
					else { // Normal source completed or user unsubscribed
						context.untilSubscription.cancelImmediately();
						subscriber.onComplete(completion);
					}
				}
			}
		});
	}
}
