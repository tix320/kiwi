package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.common.item.LastItem;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> extends TransformObservable<T> {

	private final Observable<T> observable;

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		if (count < 0) {
			throw new IllegalArgumentException("Count must not be negative");
		}
		this.observable = observable;
		this.count = count;
	}

	@Override
	public Subscription particularSubscribe(ConditionalConsumer<? super Item<? extends T>> consumer,
											ConditionalConsumer<Throwable> errorHandler) {
		if (count == 0) {
			return () -> {
			};
		}
		AtomicLong limit = new AtomicLong(count);
		return observable.particularSubscribe(item -> {
			long remaining = limit.decrementAndGet();
			if (remaining > 0) {
				return consumer.consume(item);
			}
			else if (remaining == 0) {
				consumer.consume(new LastItem<>(item.get()));
			}
			return false;
		}, errorHandler);
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
