package io.titix.kiwi.rx.internal.observable.filter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observable.BaseObservable;

/**
 * @author tix32 on 24-Feb-19
 */
abstract class FilterObservable<T, R> extends BaseObservable<R> {

	private final BaseObservable<T> observable;

	FilterObservable(Observable<T> observable) {
		if (observable instanceof BaseObservable) {
			this.observable = (BaseObservable<T>) observable;
		}
		else {
			throw new IllegalArgumentException("observable type must be " + BaseObservable.class.getName());
		}
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
