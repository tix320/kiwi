package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.signal.SignalManager;

/**
 * @author Tigran Sargsyan on 13-Nov-21.
 */
public final class FirstOfAllObservable<T> extends MonoObservable<T> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("FIRST_OF_ALL_COMPLETED");

	private final List<Observable<? extends T>> observables;

	public FirstOfAllObservable(List<Observable<? extends T>> observables) {
		if (observables.isEmpty()) {
			throw new IllegalArgumentException("Empty observables");
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		int observablesCount = observables.size();

		SignalManager signalManager = subscriber.getSignalManager();
		signalManager.increaseTokensCount(observablesCount - 1);

		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

		AtomicInteger completedCount = new AtomicInteger(0);

		Subscription generalSubscription = new Subscription() {

			private final AtomicBoolean requested = new AtomicBoolean(false);

			@Override
			protected void onRequest(long count) {
				// don't care about count, because this observable emits only one item
				boolean changed = requested.compareAndSet(false, true);
				if (changed) {
					for (Subscription subscription : subscriptions) {
						subscription.request(1);
					}
				}
			}

			@Override
			protected void onUnboundRequest() {
				onRequest(1);
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
				UserUnsubscription userUnsubscription = new UserUnsubscription(unsubscription);
				for (Subscription subscription : subscriptions) {
					subscription.cancel(userUnsubscription);
				}
			}
		};

		for (Observable<? extends T> observable : observables) {
			observable.subscribe(new Subscriber<T>(signalManager) {
				@Override
				protected void onSubscribe(Subscription subscription) {
					subscriptions.add(subscription);

					if (subscriptions.size() == observablesCount) {
						subscriber.setSubscription(generalSubscription);
					}
				}

				@Override
				protected void onNext(T item) {
					subscriber.publish(item);
					for (Subscription subscription : subscriptions) {
						subscription.cancel();
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
				protected void onComplete(Completion completion) {
					if (completedCount.incrementAndGet() == observablesCount) {
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

	private static final class UserUnsubscription extends Unsubscription {

		public UserUnsubscription(Unsubscription data) {
			super(data);
		}

		public Unsubscription realUnsubscription() {
			return data();
		}
	}
}
