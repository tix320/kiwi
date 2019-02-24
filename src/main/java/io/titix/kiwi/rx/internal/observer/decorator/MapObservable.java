package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.function.Function;
import java.util.function.Predicate;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.Filter;

/**
 * @author tix32 on 24-Feb-19
 */
public final class MapObservable<T, R> extends DecoratorObservable<T> {

	private final Function<T, R> mapper;

	public MapObservable(Observable<T> observable, Function<T, R> mapper) {
		super(observable);
		this.mapper = mapper;
	}

	@Override
	Predicate<Filter> filter() {
		return filter -> filter. ; // TODO decorator may change object
	}
}
