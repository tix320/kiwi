package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
public final class MapObservable<T, R> extends FilterObservable<T, R> {

	private final Function<T, R> mapper;

	public MapObservable(Observable<T> observable, Function<T, R> mapper) {
		super(observable);
		this.mapper = mapper;
	}


	@Override
	BiFunction<Subscription, T, Result<R>> filter() {
		return (subscription, object) -> Result.forNext(mapper.apply(object));
	}
}
