package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.function.Predicate;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class TakeWhileObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public TakeWhileObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = observable;
		this.filter = filter;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<>() {

			private volatile boolean completedByPredicate = false;

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				if (filter.test(item)) {
					return subscriber.onPublish(item);
				}
				else {
					completedByPredicate = true;
					return false;
				}
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (completedByPredicate) {
					subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
				}
				else {
					subscriber.onComplete(completionType);
				}
			}
		});
	}
}
