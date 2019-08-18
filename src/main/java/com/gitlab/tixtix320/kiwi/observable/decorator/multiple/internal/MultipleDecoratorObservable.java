package com.gitlab.tixtix320.kiwi.observable.decorator.multiple.internal;

import java.util.concurrent.atomic.AtomicInteger;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.decorator.DecoratorObservable;

public abstract class MultipleDecoratorObservable<T> extends DecoratorObservable<T> {

	final Observable<T>[] observables;

	MultipleDecoratorObservable(Observable<T>[] observables) {
		this.observables = observables;
	}

	@Override
	public final Subscription onComplete(Runnable runnable) {
		AtomicInteger count = new AtomicInteger(observables.length);
		Subscription[] subscriptions = new Subscription[observables.length];
		for (int i = 0; i < observables.length; i++) {
			Subscription subscription = observables[i].onComplete(() -> {
				if (count.decrementAndGet() == 0) {
					runnable.run();
				}
			});
			subscriptions[i] = subscription;
		}

		return () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
	}
}
