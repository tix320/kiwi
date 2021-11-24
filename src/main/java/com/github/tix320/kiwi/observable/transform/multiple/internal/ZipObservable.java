package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.demand.DemandStrategy;
import com.github.tix320.kiwi.observable.demand.EmptyDemandStrategy;
import com.github.tix320.kiwi.observable.demand.InfiniteDemandStrategy;
import com.github.tix320.kiwi.observable.signal.SignalManager;

public final class ZipObservable<T> extends Observable<List<T>> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("ZIP_ALL_COMPLETED");

	private static final SourceCompletion COMPLETED_BY_ONE_WITH_NO_ITEMS = new SourceCompletion(
			"ZIP_COMPLETED_BY_ONE_WITH_NO_ITEMS");

	private static final Unsubscription UNSUBSCRIPTION_BECAUSE_OF_SOME_COMPLETE = new Unsubscription(
			"UNSUBSCRIPTION_BECAUSE_OF_SOME_COMPLETE");

	@SuppressWarnings("unchecked")
	private final T nullObj = (T) new Object();

	private final List<Observable<? extends T>> observables;

	public ZipObservable(List<Observable<? extends T>> observables) {
		if (observables.isEmpty()) {
			throw new IllegalArgumentException("Empty observables");
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		int observablesCount = observables.size();

		AtomicReferenceArray<SourceInfo<T>> sourceInfos = createStorage(observablesCount);

		List<Subscription> subscriptions = new ArrayList<>(observables.size());

		AtomicInteger readyCount = new AtomicInteger(0);

		AtomicInteger completedCount = new AtomicInteger(0);

		Subscription generalSubscription = new Subscription() {

			private volatile DemandStrategy demandStrategy = EmptyDemandStrategy.INSTANCE;

			@Override
			protected void onRequest(long count) {
				boolean wasNeedBeforeUpdate = demandStrategy.next();
				//noinspection NonAtomicOperationOnVolatileField
				demandStrategy = demandStrategy.addBound(count);

				if (!wasNeedBeforeUpdate) {
					for (Subscription subscription : subscriptions) {
						subscription.request(1);
					}
				}
			}

			@Override
			protected void onUnboundRequest() {
				boolean wasNeedBeforeUpdate = demandStrategy.next();
				demandStrategy = InfiniteDemandStrategy.INSTANCE;

				if (!wasNeedBeforeUpdate) {
					for (Subscription subscription : subscriptions) {
						subscription.request(1);
					}
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
					SourceInfo<T> sourceInfo = sourceInfos.get(index);

					sourceInfo.item = item;
					int count = readyCount.incrementAndGet();

					if (count == observablesCount) {
						boolean needCompleteAll = false;

						List<T> zip = new ArrayList<>(observablesCount);

						for (int j = 0; j < sourceInfos.length(); j++) {
							SourceInfo<T> info = sourceInfos.get(j);
							T queueItem = info.item;
							zip.add(queueItem);

							if (info.completed) {
								needCompleteAll = true;
							}
						}

						subscriber.publish(zip);

						if (needCompleteAll) {
							for (Subscription subscription : subscriptions) {
								subscription.cancel(UNSUBSCRIPTION_BECAUSE_OF_SOME_COMPLETE);
							}
						}
						else {
							readyCount.set(0);
							for (Subscription subscription : subscriptions) {
								subscription.request(1);
							}
						}
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
						SourceInfo<T> sourceInfo = sourceInfos.get(index);
						if (sourceInfo.item == nullObj) {
							subscriber.complete(COMPLETED_BY_ONE_WITH_NO_ITEMS);
							for (Subscription subscription : subscriptions) {
								subscription.cancel();
							}
						}
						else {
							sourceInfo.completed = true;
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

	private AtomicReferenceArray<SourceInfo<T>> createStorage(int length) {
		AtomicReferenceArray<SourceInfo<T>> storage = new AtomicReferenceArray<>(length);

		for (int i = 0; i < length; i++) {
			storage.set(i, new SourceInfo<>(nullObj, false));
		}

		return storage;
	}

	private static final class SourceInfo<T> {
		volatile T item;
		volatile boolean completed;

		public SourceInfo(T item, boolean completed) {
			this.item = item;
			this.completed = completed;
		}
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
