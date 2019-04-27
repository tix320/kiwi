package com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.internal;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends TransformObservable<T, T> {

	private final Predicate<? super T> filter;

	public FilterObservable(BaseObservable<T> observable, Predicate<? super T> filter) {
		super(observable);
		this.filter = filter;
	}

	@Override
	protected BiFunction<Subscription, T, Optional<T>> transformer() {
		return (subscription, object) -> {
			if (filter.test(object)) {
				return Optional.of(object);
			}
			return Optional.empty();
		};
	}
}
