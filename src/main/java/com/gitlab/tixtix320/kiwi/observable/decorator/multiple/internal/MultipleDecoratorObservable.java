package com.gitlab.tixtix320.kiwi.observable.decorator.multiple.internal;

import java.util.concurrent.atomic.AtomicInteger;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.decorator.DecoratorObservable;

public abstract class MultipleDecoratorObservable<T> extends DecoratorObservable<T> {

	final Observable<T>[] observables;

	MultipleDecoratorObservable(Observable<T>[] observables) {
		this.observables = observables;
	}

	@Override
	public final void onComplete(Runnable runnable) {
		AtomicInteger count = new AtomicInteger(observables.length);
		for (Observable<T> observable : observables) {
			observable.onComplete(() -> {
				if (count.decrementAndGet() == 0) {
					runnable.run();
				}
			});
		}
	}
}
