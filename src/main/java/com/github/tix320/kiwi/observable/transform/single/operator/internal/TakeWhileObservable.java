package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.function.Predicate;

import com.github.tix320.kiwi.observable.*;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class TakeWhileObservable<T> extends Observable<T> {

	private static final Unsubscription PREDICATE_UNSUBSCRIPTION = new Unsubscription("PREDICATE_UNSUBSCRIPTION");

	private static final SourceCompletion SOURCE_COMPLETED_BY_PREDICATE = new SourceCompletion(
			"SOURCE_COMPLETED_BY_PREDICATE");

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public TakeWhileObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = observable;
		this.filter = filter;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<T>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(T item) {
				if (filter.test(item)) {
					subscriber.publish(item);
				}
				else {
					subscription().cancel(PREDICATE_UNSUBSCRIPTION);
				}
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == PREDICATE_UNSUBSCRIPTION) {
					subscriber.complete(SOURCE_COMPLETED_BY_PREDICATE);
				}
				else {
					subscriber.complete(completion);
				}
			}
		});
	}
}
