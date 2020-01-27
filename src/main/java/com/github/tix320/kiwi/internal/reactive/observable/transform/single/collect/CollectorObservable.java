package com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.common.item.LastItem;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends TransformObservable<R> {

	private final Observable<S> observable;

	private final Queue<S> objects;

	CollectorObservable(Observable<S> observable) {
		this.observable = observable;
		this.objects = new LinkedList<>();
	}

	@Override
	public final Subscription particularSubscribe(ConditionalConsumer<? super Item<? extends R>> consumer,
												  ConditionalConsumer<Throwable> errorHandler) {
		Subscription subscription = observable.particularSubscribe(item -> {
			objects.add(item.get());
			return true;
		}, errorHandler);
		observable.onComplete(() -> {
			consumer.consume(new LastItem<>(collect(objects.stream())));
			objects.clear();
		});
		return subscription;
	}

	protected abstract R collect(Stream<S> objects);

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
