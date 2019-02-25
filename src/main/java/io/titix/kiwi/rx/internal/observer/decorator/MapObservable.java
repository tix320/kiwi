package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.BaseObservable;
import io.titix.kiwi.rx.internal.observer.Filter;

/**
 * @author tix32 on 24-Feb-19
 */
public final class MapObservable<T, R> extends DecoratorObservable<R> {

	private final Function<T, R> mapper;

	public MapObservable(Observable<T> observable, Function<T, R> mapper) {
		super(observable);
		this.mapper = mapper;
	}


	@Override
	public Subscription subscribe(Consumer<R> consumer) {
		Consumer<R> filteredConsumer = object -> consumer.accept(mapper.apply(object));
		observable.subscribe(filteredConsumer);

		return () -> observers().remove(filteredConsumer);
	}
}
