package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends Observable<T> {

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public FilterObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = Objects.requireNonNull(observable);
		this.filter = Objects.requireNonNull(filter);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(subscriber.fork(new MinorSubscriber<T, T>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(T item) {
				boolean accepted;
				try {
					accepted = filter.test(item);
				} catch (Exception exception) {
					replaceDemandAndRethrow(exception);
					return;
				}

				if (accepted) {
					subscriber.publish(item);
				} else {
					subscription().request(1);
				}
			}

			@Override
			public void onComplete(Completion completion) {
				subscriber.complete(completion);
			}
		}));
	}

}
