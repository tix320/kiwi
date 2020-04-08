package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.function.Predicate;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public FilterObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = observable;
		this.filter = filter;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				if (filter.test(item)) {
					return subscriber.onPublish(item);
				}
				return true;
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete(CompletionType completionType) {
				subscriber.onComplete(completionType);
			}
		});
	}
}
