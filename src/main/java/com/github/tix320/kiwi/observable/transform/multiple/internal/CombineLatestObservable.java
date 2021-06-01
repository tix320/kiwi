package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.github.tix320.kiwi.observable.*;

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

		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
		@SuppressWarnings("unchecked")
		T[] lastItems = (T[]) new Object[observablesCount];


		final Consumer<Completion> cleanup = completion -> {
			synchronized (subscriber) {
				for (Subscription subscription : subscriptions) {
					subscription.unsubscribe();
				}
				subscriber.onComplete(completion);
			}
		};

		AtomicInteger subscribed = new AtomicInteger(0);
		AtomicInteger completed = new AtomicInteger(0);

		for (int i = 0; i < observables.size(); i++) {
			Observable<? extends T> observable = observables.get(i);
			int index = i;
			observable.subscribe(new Subscriber<T>() {

				@Override
				public void onSubscribe(Subscription subscription) {
					subscription.freeze();
					subscriptions.add(subscription);

					if (subscribed.incrementAndGet() == observablesCount) {

						Subscription baseSubscription = new Subscription() {
							@Override
							public void freeze() {
								subscriptions.forEach(Subscription::freeze);
							}

							@Override
							public void unfreeze() {
								subscriptions.forEach(Subscription::unfreeze);
							}

							@Override
							public void unsubscribe(Unsubscription unsubscription) {
								cleanup.accept(unsubscription);
							}
						};

						subscriber.onSubscribe(baseSubscription);

						subscriptions.forEach(Subscription::unfreeze);
					}
				}

				@Override
				public void onPublish(T item) {
					Objects.requireNonNull(item,
							"Null values not allowed in " + CombineLatestObservable.class.getSimpleName());

					synchronized (subscriber) {
						lastItems[index] = item;

						for (Object lastItem : lastItems) {
							if (lastItem == null) {
								return null;
							}
						}

						List<T> combinedObjects = List.of(lastItems);
						final RegularUnsubscription regularUnsubscription = subscriber.onPublish(combinedObjects);

						if (regularUnsubscription != null) {
							cleanup.accept(regularUnsubscription);
							return regularUnsubscription;
						}

						return null;
					}
				}

				@Override
				public void onComplete(Completion completion) {
					if (completion instanceof SourceCompleted) {
						if (completed.incrementAndGet() == observablesCount) {
							cleanup.accept(SourceCompleted.DEFAULT);
						}
					}
					// If value is UNSUBSCRIPTION, this is because of cleanup has been run, no need action
				}
			});
		}
	}
}
