package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.Collection;
import java.util.Collections;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.common.item.LastItem;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends TransformObservable<T> implements MonoObservable<T> {

	private final Observable<T> observable;

	public OnceObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends T>> consumer) {
		return observable.subscribeAndHandle(item -> {
			consumer.consume(new LastItem<>(item.get()));
			return false;
		});
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
