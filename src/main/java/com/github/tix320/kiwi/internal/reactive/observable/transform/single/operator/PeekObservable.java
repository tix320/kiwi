package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class PeekObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final Consumer<? super T> action;

	public PeekObservable(Observable<T> observable, Consumer<? super T> action) {
		this.observable = observable;
		this.action = action;
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
				action.accept(item);
				return subscriber.onPublish(item);
			}

			@Override
			public void onComplete(CompletionType completionType) {
				subscriber.onComplete(completionType);
			}
		});
	}
}
