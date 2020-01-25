package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import java.util.Collection;
import java.util.Collections;

import com.github.tix320.kiwi.api.observable.*;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends DecoratorObservable<T> {

	private final BaseObservable<T> observable;

	public OnceObservable(BaseObservable<T> observable) {
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
