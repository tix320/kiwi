package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends TransformObservable<T> {

	private final Observable<T> observable;

	private final AtomicBoolean completed;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.completed = new AtomicBoolean();
		until.onComplete(() -> completed.set(true));
	}

	@Override
	public Subscription particularSubscribe(ConditionalConsumer<? super Item<? extends T>> consumer,
											ConditionalConsumer<Throwable> errorHandler) {
		if (completed.get()) {
			return () -> {
			};
		}
		return observable.particularSubscribe(item -> {
			if (completed.get()) {
				return false;
			}
			else {
				return consumer.consume(item);
			}
		}, errorHandler);
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
