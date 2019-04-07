package io.titix.kiwi.rx.internal.observable.transform;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observable.BaseObservable;

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
	BiFunction<Subscription, T, Result<R>> transformer() {
		return (subscription, object) -> Result.of(mapper.apply(object));
	}
}
