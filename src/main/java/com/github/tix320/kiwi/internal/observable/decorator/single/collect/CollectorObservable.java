package com.github.tix320.kiwi.internal.observable.decorator.single.collect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.observable.*;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends DecoratorObservable<R> {

	private final Observable<S> observable;

	private final Queue<S> objects;

	CollectorObservable(Observable<S> observable) {
		this.observable = observable;
		this.objects = new LinkedList<>();
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends R>> consumer) {
		Subscription subscription = observable.subscribeAndHandle(item -> {
			objects.add(item.get());
			return true;
		});
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
