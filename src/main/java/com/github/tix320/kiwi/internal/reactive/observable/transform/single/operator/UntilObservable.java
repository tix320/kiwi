package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final Observable<?> until;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.until = until;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();
		final AtomicBoolean untilCompleted = new AtomicBoolean(false);

		until.subscribe(Subscriber.builder().onComplete((completionType) -> {
			untilCompleted.set(true);
			Subscription subscription = subscriptionHolder.get();
			if (subscription != null) {
				subscription.unsubscribe();
			}
		}));
		observable.subscribe(new Subscriber<T>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriptionHolder.set(subscription);
				subscriber.onSubscribe(
						subscription); // TODO in case of user unsubscription, until subscription not deleted
				if (untilCompleted.get()) {
					subscription.unsubscribe();
				}
			}

			@Override
			public boolean onPublish(T item) {
				if (untilCompleted.get()) {
					return false;
				}

				return subscriber.onPublish(item);
			}

			@Override
			public boolean onError(Throwable throwable) {
				if (untilCompleted.get()) {
					return false;
				}

				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (untilCompleted.get()) {
					subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
				}
				else {
					subscriber.onComplete(completionType);
				}
			}
		});
	}
}
