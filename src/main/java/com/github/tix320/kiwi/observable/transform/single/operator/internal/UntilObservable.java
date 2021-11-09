package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.signal.SignalManager;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends Observable<T> {

	private static final Unsubscription UNTIL_UNSUBSCRIPTION = new Unsubscription();

	private static final SourceCompletion SOURCE_COMPLETED_VIA_UNTIL = new SourceCompletion(
			"SOURCE_COMPLETED_VIA_UNTIL");

	private final Observable<T> observable;

	private final Observable<?> until;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.until = until;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		var context = new Object() {
			private volatile Subscription untilSubscription;
		};

		SignalManager signalManager = subscriber.getSignalManager();
		signalManager.increaseTokensCount(1);

		observable.subscribe(new Subscriber<>(signalManager) {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);

				until.subscribe(new Subscriber<Object>(signalManager) {
					@Override
					public void onSubscribe(Subscription subscription) {
						context.untilSubscription = subscription;
					}

					@Override
					public void onNext(Object item) {

					}

					@Override
					public void onComplete(Completion completion) {
						if (completion instanceof SourceCompletion) {
							subscription.cancel(UNTIL_UNSUBSCRIPTION);
						}
					}
				});
			}

			@Override
			public void onNext(T item) {
				subscriber.publish(item);
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == UNTIL_UNSUBSCRIPTION) {
					subscriber.complete(SOURCE_COMPLETED_VIA_UNTIL);
				}
				else { // Normal source completed or user unsubscribed
					context.untilSubscription.cancel();
					subscriber.complete(completion);
				}
			}
		});
	}
}
