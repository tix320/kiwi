package io.titix.kiwi.rx.observable.transform.internal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.internal.BaseObservable;
import io.titix.kiwi.rx.observable.transform.Result;

/**
 * @author tix32 on 22-Feb-19
 */
public final class OnceObservable<T> extends TransformObservable<T, T> {

	public OnceObservable(BaseObservable<T> observable) {
		super(observable);
	}

	@Override
	protected BiFunction<Subscription, T, Result<T>> transformer() {
		AtomicBoolean need = new AtomicBoolean(true);
		return (subscription, object) -> {
			if (need.getAndSet(false)) {
				return Result.of(object);
			}
			else {
				subscription.unsubscribe();
				return Result.none();
			}
		};
	}


}
