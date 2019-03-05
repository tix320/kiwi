package io.titix.kiwi.rx.internal.observable.decorator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 22-Feb-19
 */
public final class CountingObservable<T> extends DecoratorObservable<T, T> {

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		super(observable);
		this.count = count < 1 ? 1 : count;
	}

	@Override
	BiFunction<Subscription, T, Result<T>> transformer() {
		AtomicLong limit = new AtomicLong(count);
		return (subscription, object) -> {
			if (limit.getAndDecrement() > 0) {
				return Result.of(object);
			}
			else {
				subscription.unsubscribe();
				return Result.empty();
			}
		};
	}
}
