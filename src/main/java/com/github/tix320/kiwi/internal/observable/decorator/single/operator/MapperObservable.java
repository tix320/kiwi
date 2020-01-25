package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import com.github.tix320.kiwi.api.observable.*;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> extends DecoratorObservable<R> {

	private final Observable<S> observable;

	private final Function<? super S, ? extends R> mapper;

	public MapperObservable(Observable<S> observable, Function<? super S, ? extends R> mapper) {
		this.observable = observable;
		this.mapper = mapper;
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends R>> consumer) {
		return observable.subscribeAndHandle(item -> {
			R mappedValue = mapper.apply(item.get());
			return consumer.consume(new RegularItem<>(mappedValue));
		});
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
