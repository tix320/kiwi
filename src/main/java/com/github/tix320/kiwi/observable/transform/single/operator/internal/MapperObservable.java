package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.function.Function;

import com.github.tix320.kiwi.observable.*;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> implements TransformObservable<S, R> {

	private final Observable<S> observable;

	private final Function<? super S, ? extends R> mapper;

	public MapperObservable(Observable<S> observable, Function<? super S, ? extends R> mapper) {
		this.observable = observable;
		this.mapper = mapper;
	}

	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		observable.subscribe(new Subscriber<S>() {
			@Override
			public boolean onSubscribe(Subscription subscription) {
				return subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(S item) {
				return subscriber.onPublish(mapper.apply(item));
			}

			@Override
			public void onComplete(CompletionType completionType) {
				subscriber.onComplete(completionType);
			}
		});
	}
}
