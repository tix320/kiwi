package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.function.Function;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> extends TransformObservable<R> {

	private final Observable<S> observable;

	private final Function<? super S, ? extends R> mapper;

	public MapperObservable(Observable<S> observable, Function<? super S, ? extends R> mapper) {
		this.observable = observable;
		this.mapper = mapper;
	}

	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		observable.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(S item) {
				return subscriber.onPublish(mapper.apply(item));
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
