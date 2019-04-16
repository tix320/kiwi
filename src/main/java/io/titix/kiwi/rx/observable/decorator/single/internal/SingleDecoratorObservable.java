package io.titix.kiwi.rx.observable.decorator.single.internal;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.observable.decorator.DecoratorObservable;

/**
 * @param <S> source type
 * @param <R> result type
 */
public abstract class SingleDecoratorObservable<S, R> extends DecoratorObservable<R> {

	protected final Observable<S> observable;

	public SingleDecoratorObservable(Observable<S> observable) {
		this.observable = observable;
	}

	@Override
	public final void onComplete(Runnable runnable) {
		observable.onComplete(runnable);
	}
}
