package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.concurrent.atomic.AtomicLong;
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
public abstract class DecoratorObservable<T> extends BaseObservable<T> {

	protected final Observable<T> observable;

	protected DecoratorObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public final Subscription subscribe(Consumer<T> consumer) {
		return ((BaseObservable<T>) observable).subscribe(object -> {

			AtomicLong limit = new AtomicLong(4);

			return new Filter<>() {
				@Override
				public boolean finish() {
					return limit.get() == 0;
				}

				@Override
				public boolean needMore() {
					return limit.getAndDecrement() == 0;
				}

				@Override
				public void consume(T object) {
					consumer.accept(object);
				}
			};
		});
	}

	@Override
	public Subscription subscribe(Function<T, Filter<T>> filterFactory) {
		((BaseObservable<T>) observable).subscribe(objec -> );
		return null;
	}

	abstract Predicate<Filter> filter();

}
