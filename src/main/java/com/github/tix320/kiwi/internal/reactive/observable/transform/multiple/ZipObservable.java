package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public final class ZipObservable<T> extends TransformObservable<List<T>> {

	private final List<Observable<T>> observables;

	public ZipObservable(List<Observable<T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = observables;
	}

	@Override
	public Subscription subscribe(Subscriber<? super List<T>> subscriber) {
		List<Queue<T>> queues = new ArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			queues.add(new LinkedList<>());
		}

		AtomicBoolean completed = new AtomicBoolean(false);
		List<Subscription> subscriptions = new ArrayList<>(observables.size());
		Runnable cleanup = () -> {
			queues.forEach(Collection::clear);
			queues.clear();
			subscriptions.forEach(Subscription::unsubscribe);
			subscriber.onComplete();
		};

		for (int i = 0; i < observables.size(); i++) {
			Observable<T> observable = observables.get(i);
			Queue<T> queue = queues.get(i);
			observable.subscribe(new Subscriber<>() {
				private Subscription subscription;

				@Override
				public void onSubscribe(Subscription subscription) {
					subscriptions.add(subscription);
					this.subscription = subscription;
				}

				@Override
				public boolean onPublish(T item) {
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
					boolean needMore = subscriber.onPublish(combinedObjects);

					if (completed.get()) {
						for (int j = 0; j < queues.size(); j++) {
							Queue<T> q = queues.get(j);
							Subscription subscription = subscriptions.get(j);
							if (subscription.isCompleted() && q.isEmpty()) {
								cleanup.run();
								break;
							}
						}
					}

					return needMore;
				}

				@Override
				public boolean onError(Throwable throwable) {
					return subscriber.onError(throwable);
				}

				@Override
				public void onComplete() {
					subscription.unsubscribe();
					if (completed.compareAndSet(false, true) && queue.isEmpty()) {
						cleanup.run();
					}
				}
			});
		}

		Subscription subscription = new Subscription() {
			@Override
			public boolean isCompleted() {
				return completed.get();
			}

			@Override
			public void unsubscribe() {
				if (completed.compareAndSet(false, true)) {
					cleanup.run();
				}
			}
		};
		subscriber.onSubscribe(subscription);
		return subscription;
	}
}
