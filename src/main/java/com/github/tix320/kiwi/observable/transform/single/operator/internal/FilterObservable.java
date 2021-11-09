package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.function.Predicate;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends Observable<T> {

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public FilterObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = observable;
		this.filter = filter;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<>(subscriber.getSignalManager()) {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(T item) {
				if (filter.test(item)) {
					subscriber.publish(item);
				}
			}

			@Override
			public void onComplete(Completion completion) {
				subscriber.complete(completion);
			}
		});
	}
}
