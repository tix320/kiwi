package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.concurrent.atomic.AtomicReference;
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
	public final Subscription subscribe(Consumer<? super R> consumer) {
		AtomicReference<Subscription> subscription = new AtomicReference<>();
		BiFunction<Subscription, T, Result<R>> filter = filter();
		subscription.set(observable.subscribe(object -> {
			Result<R> result = filter.apply(() -> {
				Subscription sub = subscription.get();
				if (sub != null) {
					sub.unsubscribe();
				}
			}, object);
			if (!result.done) {
				consumer.accept(result.object);
			}

		}));
		return subscription.get();
	}

	@Override
	public final void onComplete(Runnable runnable) {
		observable.onComplete(runnable);
	}

	abstract BiFunction<Subscription, T, Result<R>> filter();
}
