package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.*;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.common.item.RegularItem;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public final class CombineObservable<T> extends TransformObservable<List<T>> {

	private final List<Observable<T>> observables;

	public CombineObservable(List<Observable<T>> observables) {
		this.observables = observables;
	}

	@Override
	public Subscription particularSubscribe(ConditionalConsumer<? super Item<? extends List<T>>> consumer,
											ConditionalConsumer<Throwable> errorHandler) {
		Subscription[] subscriptions = new Subscription[observables.size()];
		List<Queue<T>> queues = new ArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			queues.add(new LinkedList<>());
		}
		for (int i = 0; i < observables.size(); i++) {
			Observable<T> observable = observables.get(i);
			Queue<T> queue = queues.get(i);
			Subscription subscription = observable.particularSubscribe(item -> {
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
			}, errorHandler);

			subscriptions[i] = subscription;
		}

		Subscription combinedSubscription = () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};

		for (Observable<T> observable : observables) {
			observable.onComplete(combinedSubscription::unsubscribe);
		}

		return combinedSubscription;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return (Collection) observables;
	}
}
