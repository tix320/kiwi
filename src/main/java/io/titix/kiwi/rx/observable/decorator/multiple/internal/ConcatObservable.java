package io.titix.kiwi.rx.observable.decorator.multiple.internal;

import java.util.function.Consumer;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.internal.BaseObservable;

/**
 * @author tix32 on 24-Feb-19
 */
public final class ConcatObservable<T> extends MultipleDecoratorObservable<T> {

	public ConcatObservable(BaseObservable<T>[] observables) {
		super(observables);
	}

	@Override
	public Subscription subscribe(Consumer<? super T> consumer) {
		Subscription[] subscriptions = new Subscription[observables.length];
		for (int i = 0; i < observables.length; i++) {
			subscriptions[i] = observables[i].subscribe(consumer);
		}
		return () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
	}

}
