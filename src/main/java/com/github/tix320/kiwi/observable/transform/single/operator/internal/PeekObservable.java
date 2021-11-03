package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.function.Consumer;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class PeekObservable<T> extends Observable<T> {

	private final Observable<T> observable;

	private final Consumer<? super T> action;

	public PeekObservable(Observable<T> observable, Consumer<? super T> action) {
		this.observable = observable;
		this.action = action;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(T item) {
				action.accept(item);
				subscriber.publish(item);
			}

			@Override
			public void onComplete(Completion completion) {
				subscriber.complete(completion);
			}
		});
	}
}
