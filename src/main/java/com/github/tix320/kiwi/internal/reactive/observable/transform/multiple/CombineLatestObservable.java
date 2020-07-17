package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.*;

public final class CombineLatestObservable<T> implements TransformObservable<T, List<T>> {

	private final List<Observable<? extends T>> observables;

	public CombineLatestObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		int observablesCount = observables.size();

		AtomicBoolean completed = new AtomicBoolean(false);
		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
		List<T> lastItems = new CopyOnWriteArrayList<>();

		for (int i = 0; i < observablesCount; i++) {
			lastItems.add(null);
		}

		Consumer<CompletionType> cleanup = (completionType) -> {
			synchronized (subscriber) {
				if (completed.compareAndSet(false, true)) {
					for (Subscription subscription : subscriptions) {
						subscription.unsubscribe();
					}
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
			Observable<? extends T> observable = observables.get(i);
			int index = i;
			observable.subscribe(new Subscriber<T>() {

				@Override
				public boolean onSubscribe(Subscription subscription) {
					synchronized (subscriber) {
						if (completed.get()) {
							return false;
						}
						else {
							subscriptions.add(subscription);
							return true;
						}
					}
				}

				@Override
				public boolean onPublish(T item) {
					synchronized (subscriber) {
						lastItems.set(index, item);

						for (Object lastItem : lastItems) {
							if (lastItem == null) {
								return true;
							}
						}

						List<T> combinedObjects = new ArrayList<>(lastItems.size());
						combinedObjects.addAll(lastItems);
						boolean needMore = subscriber.onPublish(combinedObjects);

						if (!needMore) {
							cleanup.accept(CompletionType.UNSUBSCRIPTION);
							return false;
						}
						return true;
					}
				}

				@Override
				public void onComplete(CompletionType completionType) {
					if (completionType == CompletionType.SOURCE_COMPLETED) {
						cleanup.accept(CompletionType.SOURCE_COMPLETED);
					}
				}
			});
		}
	}
}
