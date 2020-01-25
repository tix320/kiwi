package com.github.tix320.kiwi.internal.reactive.observable.transform;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.internal.reactive.observable.BaseObservable;

public abstract class TransformObservable<T> extends BaseObservable<T> {

	@Override
	public final void onComplete(Runnable runnable) {
		AtomicInteger completedCount = new AtomicInteger();
		Collection<Observable<?>> observables = decoratedObservables();
		for (Observable<?> observable : observables) {
			observable.onComplete(() -> {
				if (completedCount.incrementAndGet() == observables.size()) {
					runnable.run();
				}
			});
		}
	}

	protected abstract Collection<Observable<?>> decoratedObservables();
}
