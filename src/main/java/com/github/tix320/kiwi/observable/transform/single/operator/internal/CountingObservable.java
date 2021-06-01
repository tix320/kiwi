package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> implements Observable<T> {

	private static final RegularUnsubscription COUNTING_UNSUBSCRIPTION = new RegularUnsubscription(null);

	private final Observable<T> observable;

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		if (count < 0) {
			throw new IllegalArgumentException("Count must not be negative");
		}
		this.observable = observable;
		this.count = count;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		AtomicLong limit = new AtomicLong(count);
		observable.subscribe(new Subscriber<>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public void onPublish(T item) {
				long remaining = limit.decrementAndGet();
				if (remaining > 0) {
					return subscriber.onPublish(item);
				} else {
					try {
						final RegularUnsubscription regularUnsubscription = subscriber.onPublish(item);
						if (regularUnsubscription != null) {
							return regularUnsubscription;
						}
					} catch (Throwable e) {
						ExceptionUtils.applyToUncaughtExceptionHandler(e);
					}

					return COUNTING_UNSUBSCRIPTION;
				}
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == COUNTING_UNSUBSCRIPTION) {
					subscriber.onComplete(SourceCompleted.DEFAULT);
				} else {
					subscriber.onComplete(completion);
				}
			}
		});
	}
}
