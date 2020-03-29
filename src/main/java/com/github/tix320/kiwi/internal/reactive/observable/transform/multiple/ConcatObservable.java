package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends TransformObservable<T> {

	private final List<Observable<? extends T>> observables;

	public ConcatObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = observables;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		List<Subscription> subscriptions = new ArrayList<>(observables.size());
		AtomicBoolean unsubscribed = new AtomicBoolean(false);

		Subscription generalSubscription = new Subscription() {
			@Override
			public boolean isCompleted() {
				return unsubscribed.get();
			}

			@Override
			public void unsubscribe() {
				unsubscribed.set(true);
				for (Subscription subscription : subscriptions) {
					subscription.unsubscribe();
				}
			}
		};

		subscriber.onSubscribe(generalSubscription);

		AtomicInteger completedCount = new AtomicInteger(0);

		Subscriber<? super T> generalSubscriber = new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriptions.add(subscription);
			}

			@Override
			public boolean onPublish(T item) {
				return subscriber.onPublish(item);
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (completionType == CompletionType.UNSUBSCRIPTION) {
					subscriber.onComplete(CompletionType.UNSUBSCRIPTION);
				}
				else {
					int count = completedCount.incrementAndGet();
					if (count == observables.size()) {
						subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
					}
				}
			}
		};

		for (Observable<? extends T> observable : observables) {
			if (unsubscribed.get()) {
				break;
			}
			observable.subscribe(generalSubscriber);
		}
	}
}
