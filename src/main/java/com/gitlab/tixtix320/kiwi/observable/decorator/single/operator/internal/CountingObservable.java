package com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.internal;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.Result;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> extends OperatorObservable<T, T> {

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		super(observable);
		if (count < 0) {
			throw new IllegalArgumentException("Count must not be negative");
		}
		this.count = count;
	}

	@Override
	protected Function<T, Result<T>> operator() {
		AtomicLong limit = new AtomicLong(count);
		return object -> {
			if (limit.getAndDecrement() > 0) {
				return Result.of(object);
			}
			else {
				return Result.unsubscribe();
			}
		};
	}
}
