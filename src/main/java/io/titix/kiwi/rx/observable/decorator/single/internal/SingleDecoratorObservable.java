package io.titix.kiwi.rx.observable.decorator.single.internal;

import io.titix.kiwi.rx.observable.internal.BaseObservable;
import io.titix.kiwi.rx.observable.internal.DecoratorObservable;

/**
 * @param <S> source type
 * @param <R> result type
 */
public abstract class SingleDecoratorObservable<S, R> extends DecoratorObservable<R> {

	protected final BaseObservable<S> observable;

	public SingleDecoratorObservable(BaseObservable<S> observable) {
		this.observable = observable;
	}

	@Override
	public final void onComplete(Runnable runnable) {
		observable.onComplete(runnable);
	}
}
