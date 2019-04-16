package io.titix.kiwi.rx.observable.decorator.multiple.internal;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.observable.Observer;
import io.titix.kiwi.rx.observable.ObserverWithSubscription;
import io.titix.kiwi.rx.observable.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
public final class ConcatObservable<T> extends MultipleDecoratorObservable<T> {

	public ConcatObservable(Observable<T>[] observables) {
		super(observables);
	}

	@Override
	public Subscription subscribe(Observer<? super T> observer) {
		Subscription[] subscriptions = new Subscription[observables.length];
		for (int i = 0; i < observables.length; i++) {
			subscriptions[i] = observables[i].subscribe(observer);
		}
		return () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
	}

	@Override
	public Subscription subscribeAndHandle(ObserverWithSubscription<? super T> observer) {
		Subscription[] subscriptions = new Subscription[observables.length];
		for (int i = 0; i < observables.length; i++) {
			subscriptions[i] = observables[i].subscribeAndHandle(observer);
		}
		return () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
	}

}
