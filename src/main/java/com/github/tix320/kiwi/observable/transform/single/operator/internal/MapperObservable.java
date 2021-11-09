package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.function.Function;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> extends Observable<R> {

	private final Observable<S> observable;

	private final Function<? super S, ? extends R> mapper;

	public MapperObservable(Observable<S> observable, Function<? super S, ? extends R> mapper) {
		this.observable = observable;
		this.mapper = mapper;
	}

	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		observable.subscribe(new Subscriber<>(subscriber.getSignalManager()) {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(S item) {
				subscriber.publish(mapper.apply(item));
			}

			@Override
			public void onComplete(Completion completion) {
				subscriber.complete(completion);
			}
		});
	}
}
