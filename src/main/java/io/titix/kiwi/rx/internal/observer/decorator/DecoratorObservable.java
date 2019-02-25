package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.Collection;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.internal.observer.BaseObservable;

/**
 * @author tix32 on 24-Feb-19
 */
public abstract class DecoratorObservable<T,R> extends BaseObservable<R> {

	protected final BaseObservable<T> observable;

	protected DecoratorObservable(Observable<T> observable) {
		if (observable instanceof BaseObservable) {
			this.observable = (BaseObservable<T>) observable;
		}
		else {
			throw new IllegalArgumentException("observable must be instance of " + BaseObservable.class.getSimpleName());
		}
	}

	@Override
	public final Collection<Consumer<R>> observers() {
		return observable.observers();
	}
}
