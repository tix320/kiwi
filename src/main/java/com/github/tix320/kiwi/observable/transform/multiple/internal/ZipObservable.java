package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.github.tix320.kiwi.observable.*;

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
		List<Queue<T>> queues = new CopyOnWriteArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			queues.add(new ConcurrentLinkedQueue<>());
		}

		AtomicBoolean atLeastOneObservableCompleted = new AtomicBoolean(false);
		AtomicBoolean completed = new AtomicBoolean(false);
		List<Subscription> subscriptions = new ArrayList<>(observables.size());

		Consumer<CompletionType> cleanup = (completionType) -> {
			synchronized (subscriber) {
				boolean changed = completed.compareAndSet(false, true);
				if (changed) {
					subscriptions.forEach(Subscription::unsubscribe);
					queues.forEach(Collection::clear);
					queues.clear();
					subscriber.onComplete(completionType);
				}
			}
		};

		Subscription subscription = new Subscription() {
			@Override
			public boolean isCompleted() {
				return completed.get();
			}

			@Override
			public void unsubscribe() {
				cleanup.accept(CompletionType.UNSUBSCRIPTION);
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
					synchronized (subscriber) {
						if (completed.get()) {
							return false;
						}

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

						if (!needMore) {
							cleanup.accept(CompletionType.UNSUBSCRIPTION);
							return false;
						}

						boolean needComplete = false;

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

						if (needComplete) {
							subscriptions.remove(subscription);
							cleanup.accept(CompletionType.SOURCE_COMPLETED);
							return false;
						}

						return true;
					}
				}

				@Override
				public void onComplete(CompletionType completionType) {
					synchronized (subscriber) {
						boolean needComplete = false;
						if (completionType == CompletionType.SOURCE_COMPLETED) {
							if (atLeastOneObservableCompleted.compareAndSet(false, true) && queue.isEmpty()) {
								needComplete = true;
							}
						}
						if (needComplete) {
							cleanup.accept(CompletionType.SOURCE_COMPLETED);
						}
					}
				}
			});
		}
	}
}
