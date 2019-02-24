package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 22-Feb-19
 */
public final class CountingObservable<T> extends DecoratorObservable<T> {

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		super(observable);
		this.count = count < 1 ? 1 : count;
	}

	@Override
	Predicate<Subscription> filter() {
		AtomicLong limit = new AtomicLong(count);
		return subscription -> {
			if (limit.getAndDecrement() == 0) {
				subscription.unsubscribe();
				return false;
			}
			return true;
		};
	}
}
