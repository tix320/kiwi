package com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.internal;

import java.util.Optional;
import java.util.function.BiFunction;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.ObserverWithSubscription;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.internal.SingleDecoratorObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public abstract class TransformObservable<S, R> extends SingleDecoratorObservable<S, R> {

	protected TransformObservable(Observable<S> observable) {
		super(observable);
	}

	@Override
	public final Subscription subscribe(Observer<? super R> observer) {
		BiFunction<Subscription, S, Optional<R>> transformer = transformer();
		return observable.subscribeAndHandle((object, subscription) -> {
			Optional<R> value = transformer.apply(subscription, object);
			value.ifPresent(observer::consume);
		});
	}

	@Override
	public final Subscription subscribeAndHandle(ObserverWithSubscription<? super R> observer) {
		BiFunction<Subscription, S, Optional<R>> transformer = transformer();
		return observable.subscribeAndHandle((object, subscription) -> {
			Optional<R> value = transformer.apply(subscription, object);
			value.ifPresent(resultObject -> observer.consume(resultObject, subscription));
		});
	}

	protected abstract BiFunction<Subscription, S, Optional<R>> transformer();
}
