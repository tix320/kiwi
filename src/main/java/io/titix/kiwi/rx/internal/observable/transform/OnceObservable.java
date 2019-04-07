package io.titix.kiwi.rx.internal.observable.transform;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observable.BaseObservable;

/**
 * @author tix32 on 22-Feb-19
 */
public final class OnceObservable<T> extends TransformObservable<T, T> {

	public OnceObservable(BaseObservable<T> observable) {
		super(observable);
	}

	@Override
	BiFunction<Subscription, T, Result<T>> transformer() {
		AtomicBoolean need = new AtomicBoolean(true);
		return (subscription, object) -> {
			if (need.getAndSet(false)) {
				return Result.of(object);
			}
			else {
				subscription.unsubscribe();
				return Result.empty();
			}
		};
	}


}
