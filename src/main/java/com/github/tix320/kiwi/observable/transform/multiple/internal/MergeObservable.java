package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.internal.SharedSubscriber;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MergeObservable<T> extends Observable<T> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("MERGE_ALL_COMPLETED");

	private final List<Observable<? extends T>> observables;

	public MergeObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		int observablesCount = observables.size();

		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

		Object lock = new Object();

		AtomicInteger completedCount = new AtomicInteger(0);

		AtomicBoolean userUnsubscribed = new AtomicBoolean(false);

		Subscription generalSubscription = new Subscription() {

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

					userUnsubscribed.set(true);
				}
			}
		};

		for (Observable<? extends T> observable : observables) {
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
					Objects.requireNonNull(item,
							"Null values not allowed in " + CombineLatestObservable.class.getSimpleName());
					synchronized (lock) {
						if (userUnsubscribed.get()) {
							return;
						}
						subscriber.onPublish(item);
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
						else if (completion instanceof SourceCompletion) {
							if (completedCount.incrementAndGet() == observablesCount) {
								subscriber.onComplete(ALL_COMPLETED);
							}
						}
						else {
							throw new IllegalStateException(completion.toString());
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