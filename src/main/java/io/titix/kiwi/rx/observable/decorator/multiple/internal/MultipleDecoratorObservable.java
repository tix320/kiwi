package io.titix.kiwi.rx.observable.decorator.multiple.internal;

import java.util.concurrent.atomic.AtomicInteger;

import io.titix.kiwi.rx.observable.internal.BaseObservable;
import io.titix.kiwi.rx.observable.internal.DecoratorObservable;

public abstract class MultipleDecoratorObservable<T> extends DecoratorObservable<T> {

	final BaseObservable<T>[] observables;

	MultipleDecoratorObservable(BaseObservable<T>[] observables) {
		this.observables = observables;
	}

	@Override
	public final void onComplete(Runnable runnable) {
		AtomicInteger count = new AtomicInteger(observables.length);
		for (BaseObservable<T> observable : observables) {
			observable.onComplete(() -> {
				if (count.decrementAndGet() == 0) {
					runnable.run();
				}
			});
		}
	}
}
