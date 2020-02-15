package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public final class ZipObservable<T> extends TransformObservable<List<T>> {

	private final List<Observable<T>> observables;

	public ZipObservable(List<Observable<T>> observables) {
		this.observables = observables;
	}

	@Override
	public Subscription subscribe(Subscriber<? super List<T>> subscriber) {
		Queue<Subscription> subscriptions = new LinkedList<>();
		List<Queue<T>> queues = new ArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			queues.add(new LinkedList<>());
		}

		AtomicBoolean unsubscribed = new AtomicBoolean(false);

		for (int i = 0; i < observables.size(); i++) {
			Observable<T> observable = observables.get(i);
			Queue<T> queue = queues.get(i);
			Subscription subscription = observable.subscribe(new Subscriber<T>() {
				@Override
				public boolean consume(T item) {
					queue.add(item);
					for (Queue<T> q : queues) {
						if (q.isEmpty()) {
							return true;
						}
					}

					List<T> combinedObjects = new ArrayList<>(queues.size());
					for (Queue<T> q : queues) {
						combinedObjects.add(q.poll());
					}

					return subscriber.consume(combinedObjects);
				}

				@Override
				public boolean onError(Throwable throwable) {
					return subscriber.onError(throwable);
				}

				@Override
				public void onComplete() {
					subscriptions.forEach(Subscription::unsubscribe);
					if (unsubscribed.compareAndSet(false, true)) {
						subscriber.onComplete();
					}
				}
			});

			subscriptions.add(subscription);
		}

		return () -> subscriptions.forEach(Subscription::unsubscribe);
	}
}
