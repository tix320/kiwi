package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.internal.SharedSubscriber;

public final class ZipObservable<T> extends Observable<List<T>> {

	private static final SourceCompleted ALL_COMPLETED = new SourceCompleted("ZIP_ALL_COMPLETED");

	private static final Unsubscription UNSUBSCRIPTION_BECAUSE_OF_SOME_COMPLETE = new Unsubscription(
			"UNSUBSCRIPTION_BECAUSE_OF_SOME_COMPLETE");

	private final List<Observable<? extends T>> observables;

	public ZipObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		int observablesCount = observables.size();

		List<Queue<T>> queues = new CopyOnWriteArrayList<>();
		for (int i = 0; i < observables.size(); i++) {
			queues.add(new ConcurrentLinkedQueue<>());
		}

		List<Subscription> subscriptions = new ArrayList<>(observables.size());

		AtomicInteger readyCount = new AtomicInteger(0);

		Object lock = new Object();

		AtomicInteger completedCount = new AtomicInteger(0);
		boolean[] completed = new boolean[observablesCount];

		Subscription generalSubscription = new Subscription() {

			@Override
			public void cancel(Unsubscription unsubscription) {
				throw new UnsupportedOperationException(); // TODO
			}

			@Override
			public void cancelImmediately(Unsubscription unsubscription) {
				synchronized (lock) {
					int subscriptionsSize = subscriptions.size();
					for (int i = 0; i < subscriptionsSize - 1; i++) {
						Subscription subscription = subscriptions.get(i);
						UserUnsubscription userUnsubscription = new UserUnsubscription();

						subscription.cancelImmediately(userUnsubscription);
					}

					Subscription lastSubscription = subscriptions.get(subscriptionsSize - 1);
					lastSubscription.cancelImmediately(new UserUnsubscription(unsubscription));
				}
			}
		};

		for (int i = 0; i < observables.size(); i++) {
			Observable<? extends T> observable = observables.get(i);
			int index = i;
			observable.subscribe(new SharedSubscriber<T>() {

				@Override
				public void onSubscribe(Subscription subscription) {
					subscriptions.add(subscription);

					if (subscriptions.size() == observablesCount) {
						subscriber.onSubscribe(generalSubscription);
					}
				}

				@Override
				public void onPublish(T item) {
					synchronized (lock) {

						Queue<T> queue = queues.get(index);

						boolean isEmpty = queue.isEmpty();
						queue.add(item);

						if (isEmpty) {
							int count = readyCount.incrementAndGet();

							if (count == observablesCount) {
								boolean needCompleteAll = false;

								List<T> zip = new ArrayList<>(observablesCount);
								for (int j = 0; j < queues.size(); j++) {
									Queue<T> q = queues.get(j);
									zip.add(q.remove());

									if (q.isEmpty()) {
										readyCount.decrementAndGet();

										if (completed[j]) {
											needCompleteAll = true;
										}
									}
								}

								subscriber.onPublish(zip);

								if (needCompleteAll) {
									subscriptions.forEach(subscription -> subscription.cancelImmediately(
											UNSUBSCRIPTION_BECAUSE_OF_SOME_COMPLETE));
								}
							}
						}
					}
				}

				@Override
				public void onComplete(Completion completion) {
					synchronized (lock) {
						if (completion instanceof UserUnsubscription userUnsubscription) {
							if (userUnsubscription.perform) {
								subscriber.onComplete(userUnsubscription.unsubscription);
							}
						}
						else {
							completed[index] = true;
							if (completedCount.incrementAndGet() == observablesCount) {
								subscriber.onComplete(ALL_COMPLETED);
							}
							else if (completion instanceof SourceCompleted && queues.get(index).isEmpty()) {
								subscriber.onComplete(ALL_COMPLETED);
							}
						}
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
