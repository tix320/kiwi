package com.gitlab.tixtix320.kiwi.observable.decorator.single.reduce.internal;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.internal.SingleDecoratorObservable;

public abstract class ReduceObservable<S, R> extends SingleDecoratorObservable<S, R> {

	public ReduceObservable(Observable<S> observable) {
		super(observable);
	}
}
