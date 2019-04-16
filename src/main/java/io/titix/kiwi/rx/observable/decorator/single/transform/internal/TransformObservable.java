package io.titix.kiwi.rx.observable.decorator.single.transform.internal;

import java.util.function.BiFunction;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.observable.Observer;
import io.titix.kiwi.rx.observable.ObserverWithSubscription;
import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.decorator.single.internal.SingleDecoratorObservable;
import io.titix.kiwi.rx.observable.decorator.single.transform.Result;

/**
 * @author tix32 on 24-Feb-19
 */
public abstract class TransformObservable<S, R> extends SingleDecoratorObservable<S, R> {

	protected TransformObservable(Observable<S> observable) {
		super(observable);
	}

	@Override
	public final Subscription subscribe(Observer<? super R> observer) {
		BiFunction<Subscription, S, Result<R>> transformer = transformer();
		return observable.subscribeAndHandle((object, subscription) -> {
			Result<R> result = transformer.apply(subscription, object);
			result.get().ifPresent(observer::consume);
		});
	}

	@Override
	public final Subscription subscribeAndHandle(ObserverWithSubscription<? super R> observer) {
		BiFunction<Subscription, S, Result<R>> transformer = transformer();
		return observable.subscribeAndHandle((object, subscription) -> {
			Result<R> result = transformer.apply(subscription, object);
			result.get().ifPresent(resultObject -> observer.consume(resultObject, subscription));
		});
	}

	protected abstract BiFunction<Subscription, S, Result<R>> transformer();
}
