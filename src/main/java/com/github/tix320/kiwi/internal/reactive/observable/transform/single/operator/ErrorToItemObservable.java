package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.function.Function;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran Sargsyan on 26-Apr-20.
 */
public class ErrorToItemObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final Function<Throwable, ? extends T> mapper;

	public ErrorToItemObservable(Observable<T> observable, Function<Throwable, ? extends T> mapper) {
		this.observable = observable;
		this.mapper = mapper;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<T>() {
			@Override
			public boolean onSubscribe(Subscription subscription) {
				return subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				return subscriber.onPublish(item);
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onPublish(mapper.apply(throwable));
			}

			@Override
			public void onComplete(CompletionType completionType) {
				subscriber.onComplete(completionType);
			}
		});
	}
}
