package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
abstract class FilterObservable<T, R> implements Observable<R> {

	private final Observable<T> observable;

	FilterObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public final Subscription subscribe(Consumer<R> consumer) {
		var subscription = new Object() {
			Subscription $;
		};
		BiFunction<Subscription, T, Result<R>> filter = filter();
		subscription.$ = observable.subscribe(object -> {
			Result<R> result = filter.apply(() -> {
				if (subscription.$ != null) {
					subscription.$.unsubscribe();
				}
			}, object);
			if (!result.done) {
				consumer.accept(result.object);
			}

		});
		return subscription.$;
	}

	abstract BiFunction<Subscription, T, Result<R>> filter();
}
