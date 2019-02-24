package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.function.Consumer;
import java.util.function.Predicate;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.BaseObservable;
import io.titix.kiwi.rx.internal.observer.Filter;

/**
 * @author tix32 on 24-Feb-19
 */
public abstract class DecoratorObservable<T> extends BaseObservable<T> {

	protected final Observable<T> observable;

	protected DecoratorObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public final Subscription subscribe(Consumer<T> consumer) {
		return ((BaseObservable<T>) observable).subscribe(consumer, filter());
	}

	@Override
	public final Subscription subscribe(Consumer<T> consumer, Predicate<Filter> filter) {
		return ((BaseObservable<T>) observable).subscribe(consumer, filter.and(filter()));
	}

	abstract Predicate<Filter> filter();

}
