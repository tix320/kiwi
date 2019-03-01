package io.titix.kiwi.rx.internal.observable.filter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 22-Feb-19
 */
public final class OnceObservable<T> extends FilterObservable<T, T> {

	public OnceObservable(Observable<T> observable) {
		super(observable);
	}

	@Override
	BiFunction<Subscription, T, Result<T>> filter() {
		AtomicBoolean need = new AtomicBoolean(true);
		return (subscription, object) -> {
			if (need.getAndSet(false)) {
				return Result.forNext(object);
			}
			return Result.end();
		};
	}


}
