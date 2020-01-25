package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.api.observable.*;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> extends DecoratorObservable<T> {

	private final BaseObservable<T> observable;

	private final long count;

	public CountingObservable(BaseObservable<T> observable, long count) {
		if (count < 0) {
			throw new IllegalArgumentException("Count must not be negative");
		}
		this.observable = observable;
		this.count = count;
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends T>> consumer) {
		if (count == 0) {
			return () -> {
			};
		}
		AtomicLong limit = new AtomicLong(count);
		return observable.subscribeAndHandle(item -> {
			long remaining = limit.decrementAndGet();
			if (remaining > 0) {
				return consumer.consume(item);
			}
			else if (remaining == 0) {
				consumer.consume(new LastItem<>(item.get()));
			}
			return false;
		});
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
