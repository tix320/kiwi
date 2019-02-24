package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.function.Predicate;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 22-Feb-19
 */
public final class OneTimeObservable<T> extends DecoratorObservable<T> {

	protected OneTimeObservable(Observable<T> observable) {
		super(observable);
	}

	@Override
	Predicate<Subscription> filter() {
		var consumed = new Object() {
			volatile boolean $ = false;
		};
		return (subscription) -> {
			boolean need = !consumed.$;
			consumed.$ = true;
			subscription.unsubscribe();
			return need;
		};
	}
}
