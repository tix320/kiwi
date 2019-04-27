package com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.internal;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<T, R> extends TransformObservable<T, R> {

	private final Function<? super T, ? extends R> mapper;

	public MapperObservable(BaseObservable<T> observable, Function<? super T, ? extends R> mapper) {
		super(observable);
		this.mapper = mapper;
	}

	@Override
	protected BiFunction<Subscription, T, Optional<R>> transformer() {
		return (subscription, object) -> Optional.of(mapper.apply(object));
	}
}
