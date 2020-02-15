package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends TransformObservable<T> {

	private final List<Observable<T>> observables;

	public ConcatObservable(List<Observable<T>> observables) {
		this.observables = observables;
	}

	@Override
	public Subscription subscribe(Subscriber<? super T> subscriber) {
		List<Subscription> subscriptions = new ArrayList<>(observables.size());

		AtomicBoolean unsubscribed = new AtomicBoolean(false);

		Subscription generalSubscription = () -> {
			unsubscribed.set(true);
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
		AtomicInteger completedCount = new AtomicInteger(0);

		Subscriber<? super T> generalSubscriber = new Subscriber<>() {
			@Override
			public synchronized boolean consume(T item) {
				return subscriber.consume(item);
			}

			@Override
			public synchronized boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public synchronized void onComplete() {
				int count = completedCount.incrementAndGet();
				if (count == observables.size()) {
					subscriber.onComplete();
				}
			}
		};

		for (Observable<T> observable : observables) {
			if (unsubscribed.get()) {
				break;
			}
			Subscription subscription = observable.subscribe(generalSubscriber);
			subscriptions.add(subscription);
		}

		return generalSubscription;
	}
}
