package io.titix.kiwi.rx.observable.transform.internal;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.internal.BaseObservable;
import io.titix.kiwi.rx.observable.transform.Result;

/**
 * @author tix32 on 24-Feb-19
 */
public abstract class TransformObservable<T, R> extends BaseObservable<R> {

	private final BaseObservable<T> observable;

	public TransformObservable(BaseObservable<T> observable) {
		this.observable = observable;
	}

	@Override
	public final Subscription subscribe(Consumer<? super R> consumer) {
		AtomicReference<Subscription> realSubscription = new AtomicReference<>();
		BiFunction<Subscription, T, Result<R>> transformer = transformer();
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

	@Override
	public final void onComplete(Runnable runnable) {
		observable.onComplete(runnable);
	}

	protected abstract BiFunction<Subscription, T, Result<R>> transformer();
}
