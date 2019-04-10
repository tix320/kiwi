package io.titix.kiwi.rx.observable.decorator.single.transform.internal;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.decorator.single.internal.SingleDecoratorObservable;
import io.titix.kiwi.rx.observable.decorator.single.transform.Result;
import io.titix.kiwi.rx.observable.internal.BaseObservable;

/**
 * @author tix32 on 24-Feb-19
 */
public abstract class TransformObservable<S, R> extends SingleDecoratorObservable<S, R> {

	protected TransformObservable(BaseObservable<S> observable) {
		super(observable);
	}

	@Override
	public final Subscription subscribe(Consumer<? super R> consumer) {
		AtomicReference<Subscription> realSubscription = new AtomicReference<>();
		BiFunction<Subscription, S, Result<R>> transformer = transformer();
		realSubscription.set(observable.subscribe(object -> {
			Subscription subscription = () -> {
				Subscription sub = realSubscription.get();
				if (sub != null) {
					sub.unsubscribe();
				}
			};
			Result<R> result = transformer.apply(subscription, object);
			result.get().ifPresent(consumer);
		}));
		return realSubscription.get();
	}

	protected abstract BiFunction<Subscription, S, Result<R>> transformer();
}
