package com.github.tix320.kiwi.internal.observable.decorator.multiple;

import java.util.*;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.*;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

public final class CombineObservable<T> extends DecoratorObservable<List<T>> {

	private final List<Observable<T>> observables;

	public CombineObservable(List<Observable<T>> observables) {
		this.observables = observables;
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends List<T>>> consumer) {
		Subscription[] subscriptions = new Subscription[observables.size()];
		List<Queue<T>> queues = new ArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			queues.add(new LinkedList<>());
		}
		for (int i = 0; i < observables.size(); i++) {
			Observable<T> observable = observables.get(i);
			Queue<T> queue = queues.get(i);
			Subscription subscription = observable.subscribeAndHandle(item -> {
				queue.add(item.get());

				for (Queue<T> q : queues) {
					if (q.isEmpty()) {
						return true;
					}
				}

				List<T> combinedObjects = new ArrayList<>(queues.size());
				for (Queue<T> q : queues) {
					combinedObjects.add(q.poll());
				}
				consumer.consume(new RegularItem<>(combinedObjects));
				return true;
			});

			subscriptions[i] = subscription;
		}

		return () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return (Collection) observables;
	}
}
