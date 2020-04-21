package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.*;

public final class CombineLatestObservable<T> implements TransformObservable<T, List<T>> {

	private final List<Observable<? extends T>> observables;

	public CombineLatestObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = observables;
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		List<AtomicReference<T>> lastItems = new ArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			lastItems.add(new AtomicReference<>());
		}

		AtomicBoolean completed = new AtomicBoolean(false);
		List<Subscription> subscriptions = new ArrayList<>(observables.size());

		Consumer<CompletionType> cleanup = (completionType) -> {
			completed.set(true);
			subscriptions.forEach(Subscription::unsubscribe);
			subscriber.onComplete(completionType);
			lastItems.clear();
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
			AtomicReference<T> lastItemHolder = lastItems.get(i);
			observable.subscribe(new Subscriber<T>() {

				@Override
				public void onSubscribe(Subscription subscription) {
					subscriptions.add(subscription);
				}

				@Override
				public boolean onPublish(T item) {
					lastItemHolder.set(item);
					for (AtomicReference<T> q : lastItems) {
						if (q.get() == null) {
							return true;
						}
					}

					List<T> combinedObjects = new ArrayList<>(lastItems.size());
					for (AtomicReference<T> q : lastItems) {
						combinedObjects.add(q.get());
					}
					boolean needMore = subscriber.onPublish(combinedObjects);

					if (!needMore) {
						cleanup.accept(CompletionType.UNSUBSCRIPTION);
						return false;
					}
					return true;
				}

				@Override
				public boolean onError(Throwable throwable) {
					return subscriber.onError(throwable);
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
