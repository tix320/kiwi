package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.signal.SignalManager;

public final class CombineLatestObservable<T> extends Observable<List<T>> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("COMBINE_LATEST_ALL_COMPLETED");

	private static final SourceCompletion COMPLETED_BY_ONE_WITH_NO_ITEMS = new SourceCompletion(
			"COMPLETED_BY_ONE_WITH_NO_ITEMS");

	@SuppressWarnings("unchecked")
	private final T nullObj = (T) new Object();

	private final List<Observable<? extends T>> observables;

	public CombineLatestObservable(List<Observable<? extends T>> observables) {
		if (observables.isEmpty()) {
			throw new IllegalArgumentException("Empty observables");
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		int observablesCount = observables.size();

		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

		AtomicReferenceArray<T> lastItems = createStorage(observablesCount);

		AtomicInteger readyCount = new AtomicInteger(0);

		AtomicInteger completedCount = new AtomicInteger(0);

		Subscription generalSubscription = new Subscription() {

			@Override
			protected void onRequest(long count) {
				throw new UnsupportedOperationException("Currently bound request not supported on CombineLatest");
			}

			@Override
			protected void onUnboundRequest() {
				for (Subscription subscription : subscriptions) {
					subscription.requestUnbounded();
				}
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
				UserUnsubscription userUnsubscription = new UserUnsubscription(unsubscription);
				for (Subscription subscription : subscriptions) {
					subscription.cancel(userUnsubscription);
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

					T previousItem = lastItems.getAndSet(index, item);
					if (previousItem == nullObj) {
						ready = readyCount.incrementAndGet();
					}

					if (ready == observablesCount) {
						List<T> combined = storageToList(lastItems);

						subscriber.publish(combined);
					}
				}

				@Override
				protected void onError(Throwable error) {
					for (Subscription subscription : subscriptions) {
						subscription.cancel();
					}
					subscriber.completeWithError(error);
				}

				@Override
				public void onComplete(Completion completion) {
					if (completion instanceof SourceCompletion) {
						if (lastItems.get(index) == nullObj) {
							subscriber.complete(COMPLETED_BY_ONE_WITH_NO_ITEMS);
							for (Subscription subscription : subscriptions) {
								subscription.cancel();
							}
						}
						else {
							if (completedCount.incrementAndGet() == observablesCount) {
								subscriber.complete(ALL_COMPLETED);
							}
						}
					}
					else if (completedCount.incrementAndGet() == observablesCount) {
						if (completion instanceof UserUnsubscription userUnsubscription) {
							Unsubscription realUnsubscription = userUnsubscription.realUnsubscription();
							subscriber.complete(realUnsubscription);
						}
						else {
							subscriber.complete(ALL_COMPLETED);
						}
					}
				}
			});
		}
	}

	private AtomicReferenceArray<T> createStorage(int length) {
		AtomicReferenceArray<T> storage = new AtomicReferenceArray<>(length);

		for (int i = 0; i < length; i++) {
			storage.set(i, nullObj);
		}

		return storage;
	}

	private List<T> storageToList(AtomicReferenceArray<T> storage) {
		List<T> list = new ArrayList<>(storage.length());
		for (int i = 0; i < storage.length(); i++) {
			list.add(storage.get(i));
		}

		return list;
	}

	private static final class UserUnsubscription extends Unsubscription {

		public UserUnsubscription(Unsubscription data) {
			super(data);
		}

		public Unsubscription realUnsubscription() {
			return data();
		}
	}
}
