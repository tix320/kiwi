package io.titix.kiwi.rx.observable.decorator.single.transform.internal;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.decorator.single.transform.Result;
import io.titix.kiwi.rx.observable.internal.BaseObservable;

/**
 * @author tix32 on 24-Feb-19
 */
public final class MapperObservable<T, R> extends TransformObservable<T, R> {

	private final Function<? super T, ? extends R> mapper;

	public MapperObservable(BaseObservable<T> observable, Function<? super T, ? extends R> mapper) {
		super(observable);
		this.mapper = mapper;
	}

	@Override
	protected BiFunction<Subscription, T, Result<R>> transformer() {
		return (subscription, object) -> Result.of(mapper.apply(object));
	}
}
