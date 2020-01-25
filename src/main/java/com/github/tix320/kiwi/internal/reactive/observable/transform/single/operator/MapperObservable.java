package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.common.item.RegularItem;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> extends TransformObservable<R> {

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
