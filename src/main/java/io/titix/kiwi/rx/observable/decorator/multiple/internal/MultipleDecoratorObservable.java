package io.titix.kiwi.rx.observable.decorator.multiple.internal;

import java.util.concurrent.atomic.AtomicInteger;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.observable.decorator.DecoratorObservable;

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
