package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.observable.Item;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends DecoratorObservable<T> {

	private final BaseObservable<T> observable;

	private final Predicate<? super T> filter;

	public FilterObservable(BaseObservable<T> observable, Predicate<? super T> filter) {
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
