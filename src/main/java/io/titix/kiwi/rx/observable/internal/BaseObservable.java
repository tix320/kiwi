package io.titix.kiwi.rx.observable.internal;

import io.titix.kiwi.rx.observable.Observable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

	public abstract void onComplete(Runnable runnable);
}
