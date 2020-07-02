package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.*;

public final class ZipObservable<T> implements TransformObservable<T, List<T>> {

	private final List<Observable<? extends T>> observables;

	public ZipObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		List<Queue<T>> queues = new ArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			queues.add(new LinkedList<>());
		}

		AtomicBoolean atLeastOneObservableCompleted = new AtomicBoolean(false);
		AtomicBoolean completed = new AtomicBoolean(false);
		List<Subscription> subscriptions = new ArrayList<>(observables.size());

		Consumer<CompletionType> cleanup = (completionType) -> {
			boolean changed = completed.compareAndSet(false, true);
			if (changed) {
				subscriptions.forEach(Subscription::unsubscribe);
				subscriber.onComplete(completionType);
				queues.forEach(Collection::clear);
				queues.clear();
			}
		};

		Subscription subscription = new Subscription() {
			@Override
			public boolean isCompleted() {
				return completed.get();
			}

			@Override
			public void unsubscribe() {
				if (completed.compareAndSet(false, true)) {
					cleanup.accept(CompletionType.UNSUBSCRIPTION);
				}
			}
		};
		subscriber.onSubscribe(subscription);

		for (int i = 0; i < observables.size(); i++) {
			if (completed.get()) {
				break;
			}
			Observable<? extends T> observable = observables.get(i);
			Queue<T> queue = queues.get(i);
			observable.subscribe(new Subscriber<T>() {

				private volatile Subscription subscription;

				@Override
				public boolean onSubscribe(Subscription subscription) {
					this.subscription = subscription;
					return subscriptions.add(subscription);
				}

				@Override
				public boolean onPublish(T item) {
					List<T> combinedObjects;
					synchronized (this) {
						queue.add(item);
						for (Queue<T> q : queues) {
							if (q.isEmpty()) {
								return true;
							}
						}

						combinedObjects = new ArrayList<>(queues.size());
						for (Queue<T> q : queues) {
							combinedObjects.add(q.poll());
						}
					}

					boolean needMore = subscriber.onPublish(combinedObjects);

					if (!needMore) {
						cleanup.accept(CompletionType.UNSUBSCRIPTION);
						return false;
					}

					boolean needComplete = false;

					synchronized (this) {
						if (atLeastOneObservableCompleted.get()) {
							for (int j = 0; j < queues.size(); j++) {
								Queue<T> q = queues.get(j);
								Subscription subscription = subscriptions.get(j);
								if (subscription.isCompleted() && q.isEmpty()) {
									needComplete = true;
									break;
								}
							}
						}
					}

					if (needComplete) {
						synchronized (this) {
							subscriptions.remove(subscription);
						}
						cleanup.accept(CompletionType.SOURCE_COMPLETED);
						return false;
					}

					return true;
				}

				@Override
				public void onComplete(CompletionType completionType) {
					boolean needComplete = false;
					synchronized (this) {
						if (completionType == CompletionType.SOURCE_COMPLETED) {
							if (atLeastOneObservableCompleted.compareAndSet(false, true) && queue.isEmpty()) {
								needComplete = true;
							}
						}
					}
					if (needComplete) {
						cleanup.accept(CompletionType.SOURCE_COMPLETED);
					}
				}
			});
		}
	}
}
