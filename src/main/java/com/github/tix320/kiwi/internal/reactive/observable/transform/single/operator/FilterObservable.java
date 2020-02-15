package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.function.Predicate;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends TransformObservable<T> {

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public FilterObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = observable;
		this.filter = filter;
	}

	@Override
	public Subscription subscribe(Subscriber<? super T> subscriber) {
		return observable.subscribe(new Subscriber<T>() {
			@Override
			public boolean consume(T item) {
				if (filter.test(item)) {
					subscriber.consume(item);
				}
				return true;
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete() {
				subscriber.onComplete();
			}
		});
	}
}
