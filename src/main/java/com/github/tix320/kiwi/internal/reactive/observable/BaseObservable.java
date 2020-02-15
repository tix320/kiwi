package com.github.tix320.kiwi.internal.reactive.observable;

import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

	protected BaseObservable() {
	}

	@Override
	public final Subscription subscribe(Consumer<? super T> consumer) {
		return Observable.super.subscribe(consumer);
	}

	@Override
	public final Subscription subscribe(Consumer<? super T> consumer, ConditionalConsumer<Throwable> errorHandler) {
		return Observable.super.subscribe(consumer, errorHandler);
	}

	@Override
	public final Subscription particularSubscribe(ConditionalConsumer<? super T> consumer) {
		return Observable.super.particularSubscribe(consumer);
	}

	@Override
	public final Subscription particularSubscribe(ConditionalConsumer<? super T> consumer,
												  ConditionalConsumer<Throwable> errorHandler) {
		return Observable.super.particularSubscribe(consumer, errorHandler);
	}

	@Override
	public final Subscription subscribe(Consumer<? super T> consumer, Runnable onComplete) {
		return Observable.super.subscribe(consumer, onComplete);
	}
}
