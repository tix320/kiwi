package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends TransformObservable<T> {

	private final Observable<T> observable;

	private final Predicate<? super T> filter;

	public FilterObservable(Observable<T> observable, Predicate<? super T> filter) {
		this.observable = observable;
		this.filter = filter;
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends T>> consumer) {
		return observable.subscribeAndHandle((item -> {
			if (filter.test(item.get())) {
				return consumer.consume(item);
			}
			else {
				return true;
			}
		}));
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
