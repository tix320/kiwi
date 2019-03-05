package io.titix.kiwi.rx.internal.observable.decorator;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 02-Mar-19
 */
public class FilterObservable<T> extends DecoratorObservable<T, T> {

	private final Predicate<? super T> filter;

	public FilterObservable(Observable<T> observable, Predicate<? super T> filter) {
		super(observable);
		this.filter = filter;
	}

	@Override
	BiFunction<Subscription, T, Result<T>> transformer() {
		return (subscription, object) -> {
			if (filter.test(object)) {
				return Result.of(object);
			}
			return Result.empty();
		};
	}
}
