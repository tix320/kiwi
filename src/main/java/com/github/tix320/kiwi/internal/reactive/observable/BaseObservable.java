package com.github.tix320.kiwi.internal.reactive.observable;

import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

	protected BaseObservable() {
	}

	@Override
	public final void subscribe(Consumer<? super T> consumer) {
		Observable.super.subscribe(consumer);
	}

	@Override
	public final void conditionalSubscribe(ConditionalConsumer<? super T> consumer) {
		Observable.super.conditionalSubscribe(consumer);
	}
}
