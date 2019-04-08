package io.titix.kiwi.rx.observable.transform.internal;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.internal.BaseObservable;
import io.titix.kiwi.rx.observable.transform.Result;

/**
 * @author tix32 on 02-Mar-19
 */
public final class FilterObservable<T> extends TransformObservable<T, T> {

	private final Predicate<? super T> filter;

	public FilterObservable(BaseObservable<T> observable, Predicate<? super T> filter) {
		super(observable);
		this.filter = filter;
	}

	@Override
	protected BiFunction<Subscription, T, Result<T>> transformer() {
		return (subscription, object) -> {
			if (filter.test(object)) {
				return Result.of(object);
			}
			return Result.none();
		};
	}
}
