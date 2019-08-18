package com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.internal;

import java.util.function.Function;
import java.util.function.Predicate;

import com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.Result;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends OperatorObservable<T, T> {

	private final Predicate<? super T> filter;

	public FilterObservable(BaseObservable<T> observable, Predicate<? super T> filter) {
		super(observable);
		this.filter = filter;
	}

	@Override
	protected Function<T, Result<T>> operator() {
		return (object) -> {
			if (filter.test(object)) {
				return Result.of(object);
			}
			return Result.empty();
		};
	}
}
