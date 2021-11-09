package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.signal.SignalManager;

public final class CombineLatestObservable<T> extends Observable<List<T>> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("COMBINE_LATEST_ALL_COMPLETED");

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
		Object nullObj = new Object();
		Arrays.fill(lastItems, nullObj);

		AtomicInteger readyCount = new AtomicInteger(0);

		AtomicInteger completedCount = new AtomicInteger(0);

		Subscription generalSubscription = new Subscription() {

			private final Object lock = new Object();

			@Override
			public void request(long n) { // TODO request based on last items distributive, consider MAX case
				synchronized (lock) {
					for (Subscription subscription : subscriptions) {
						subscription.request(Long.MAX_VALUE);
					}
				}
			}

			@Override
			public void cancel(Unsubscription unsubscription) {
				synchronized (lock) {
					int subscriptionsSize = subscriptions.size();
					for (int i = 0; i < subscriptionsSize - 1; i++) {
						Subscription subscription = subscriptions.get(i);
						UserUnsubscription userUnsubscription = new UserUnsubscription();

						subscription.cancel(userUnsubscription);
					}

					Subscription lastSubscription = subscriptions.get(subscriptionsSize - 1);
					lastSubscription.cancel(new UserUnsubscription(unsubscription));
				}
			}
		};

		SignalManager signalManager = subscriber.getSignalManager();
		signalManager.increaseTokensCount(observablesCount - 1);

		for (int i = 0; i < observables.size(); i++) {
			Observable<? extends T> observable = observables.get(i);
			int index = i;
			observable.subscribe(new Subscriber<T>(signalManager) {

				@Override
				public void onSubscribe(Subscription subscription) {
					subscriptions.add(subscription);

					if (subscriptions.size() == observablesCount) {
						subscriber.setSubscription(generalSubscription);
					}
				}

				@Override
				public void onNext(T item) {
					int ready = readyCount.get();

					T previousItem = lastItems[index];
					lastItems[index] = item;
					if (ready != observablesCount) {
						if (previousItem == nullObj) {
							ready = readyCount.incrementAndGet();

							if (ready != observablesCount) {
								return;
							}
						}
						else {
							return;
						}

					}

					List<T> combined = List.of(lastItems);

					subscriber.publish(combined);
				}

				@Override
				public void onComplete(Completion completion) {
					if (completion instanceof UserUnsubscription userUnsubscription) {
						if (userUnsubscription.perform) {
							subscriber.complete(userUnsubscription.unsubscription);
						}
					}
					else if (completion instanceof SourceCompletion) {
						if (completedCount.incrementAndGet() == observablesCount) {
							subscriber.complete(ALL_COMPLETED);
						}
					}
					else {
						throw new IllegalStateException(completion.toString());
					}
				}
			});
		}
	}

	private static final class UserUnsubscription extends Unsubscription {
		private final boolean perform;
		private final Unsubscription unsubscription;

		public UserUnsubscription() {
			this.perform = false;
			this.unsubscription = null;
		}

		private UserUnsubscription(Unsubscription unsubscription) {
			this.perform = true;
			this.unsubscription = unsubscription;
		}
	}
}
