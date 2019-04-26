package com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.internal;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.Result;

/**
 * @author tix32 on 22-Feb-19
 */
public final class CountingObservable<T> extends TransformObservable<T, T> {

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		super(observable);
		if (count < 0) {
			throw new IllegalArgumentException("Count must not be negative");
		}
		this.count = count;
	}

	@Override
	protected BiFunction<Subscription, T, Result<T>> transformer() {
		AtomicLong limit = new AtomicLong(count);
		return (subscription, object) -> {
			if (limit.getAndDecrement() > 0) {
				return Result.of(object);
			}
			else {
				subscription.unsubscribe();
				return Result.none();
			}
		};
	}
}
