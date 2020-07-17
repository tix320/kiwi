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
// TODO in case of user unsubscription, until subscription not deleted
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
		AtomicBoolean untilCompleted = new AtomicBoolean(false);

		until.subscribeOnComplete(completionType -> {
			synchronized (subscriber) {
				untilCompleted.set(true);

				Subscription subscription = subscriptionHolder.get();
				if (subscription != null) {
					subscription.unsubscribe();
				}
			}
		});
		observable.subscribe(new Subscriber<T>() {

			@Override
			public boolean onSubscribe(Subscription subscription) {
				synchronized (subscriber) {
					boolean needRegister = subscriber.onSubscribe(subscription);

					if (!needRegister) {
						return false;
					}

					if (untilCompleted.get()) {
						return false;
					}

					subscriptionHolder.set(subscription);
					return true;
				}
			}

			@Override
			public boolean onPublish(T item) {
				synchronized (subscriber) {
					if (untilCompleted.get()) {
						return false;
					}

					return subscriber.onPublish(item);
				}
			}

			@Override
			public void onComplete(CompletionType completionType) {
				synchronized (subscriber) {
					if (untilCompleted.get()) {
						subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
					}
					else {
						subscriber.onComplete(completionType);
					}
				}
			}
		});
	}
}
