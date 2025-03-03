package com.github.tix320.kiwi.observable.transform.multiple.internal;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends Observable<T> {

	private static final Unsubscription UNTIL_UNSUBSCRIPTION = new Unsubscription();

	private static final SourceCompletion SOURCE_COMPLETED_VIA_UNTIL = new SourceCompletion(
		"SOURCE_COMPLETED_VIA_UNTIL");

	private final Observable<T> mainSource;

	private final Observable<?> untilSource;

	public UntilObservable(Observable<T> mainSource, Observable<?> untilSource) {
		this.mainSource = mainSource;
		this.untilSource = untilSource;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		var context = new Object() {
			private volatile Subscription untilSubscription;
		};

		mainSource.subscribe(subscriber.spawn(new MinorSubscriber<T, T>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				untilSource.subscribe(subscriber.spawn(new MinorSubscriber<Object, T>() {
					@Override
					public void onSubscribe(Subscription subscription) {
						context.untilSubscription = subscription;
						subscription.requestUnbounded();
					}

					@Override
					public void onNext(Object item) {
						subscription.cancel(UNTIL_UNSUBSCRIPTION);
						context.untilSubscription.cancel();
					}

					@Override
					public void onComplete(Completion completion) {
						if (completion instanceof SourceCompletion) {
							subscription.cancel(UNTIL_UNSUBSCRIPTION);
						}
					}
				}));

				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(T item) {
				subscriber.publish(item);
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == UNTIL_UNSUBSCRIPTION) {
					subscriber.complete(SOURCE_COMPLETED_VIA_UNTIL);
				} else {
					// Normal source completed or user unsubscribed
					context.untilSubscription.cancel();
					subscriber.complete(completion);
				}
			}
		}));
	}

}
