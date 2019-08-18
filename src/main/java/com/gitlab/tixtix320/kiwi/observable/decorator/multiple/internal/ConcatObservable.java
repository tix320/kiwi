package com.gitlab.tixtix320.kiwi.observable.decorator.multiple.internal;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.Subscription;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends MultipleDecoratorObservable<T> {

	public ConcatObservable(Observable<T>[] observables) {
		super(observables);
	}

	@Override
	public Subscription subscribeAndHandle(Observer<? super T> observer) {
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
