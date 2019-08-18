package com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.internal;

import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.Result;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<T, R> extends OperatorObservable<T, R> {

	private final Function<? super T, ? extends R> mapper;

	public MapperObservable(BaseObservable<T> observable, Function<? super T, ? extends R> mapper) {
		super(observable);
		this.mapper = mapper;
	}

	@Override
	protected Function<T, Result<R>> operator() {
		return object -> Result.of(mapper.apply(object));
	}
}
