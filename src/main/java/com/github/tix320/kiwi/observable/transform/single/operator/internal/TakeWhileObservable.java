package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.function.Predicate;

import com.github.tix320.kiwi.observable.*;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class TakeWhileObservable<T> extends Observable<T> {

	private static final Unsubscription PREDICATE_UNSUBSCRIPTION = new Unsubscription("PREDICATE_UNSUBSCRIPTION");

	private static final SourceCompleted SOURCE_COMPLETED_BY_PREDICATE = new SourceCompleted(
			"SOURCE_COMPLETED_BY_PREDICATE");

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public TakeWhileObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = observable;
		this.filter = filter;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new AbstractSubscriber<>() {

			@Override
			public void onSubscribe() {
				subscriber.onSubscribe(subscription());
			}

			@Override
			public void onPublish(T item) {
				if (filter.test(item)) {
					subscriber.onPublish(item);
				}
				else {
					subscription().cancelImmediately(PREDICATE_UNSUBSCRIPTION);
				}
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == PREDICATE_UNSUBSCRIPTION) {
					subscriber.onComplete(SOURCE_COMPLETED_BY_PREDICATE);
				}
				else {
					subscriber.onComplete(completion);
				}
			}
		});
	}
}
