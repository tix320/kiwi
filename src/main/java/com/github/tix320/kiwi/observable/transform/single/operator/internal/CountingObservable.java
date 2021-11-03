package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> extends Observable<T> {

	private static final Unsubscription LIMIT_UNSUBSCRIPTION = new Unsubscription("LIMIT_UNSUBSCRIPTION");

	private static final SourceCompletion SOURCE_COMPLETED_BY_LIMIT = new SourceCompletion("SOURCE_COMPLETED_BY_LIMIT");

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
		observable.subscribe(new AbstractSubscriber<>() {

			@Override
			public void onSubscribe() {
				subscriber.onSubscribe(subscription());
			}

			@Override
			public void onPublish(T item) {
				long remaining = limit.decrementAndGet();
				if (remaining > 0) {
					subscriber.onPublish(item);
				}
				else {
					try {
						subscriber.onPublish(item);
					}
					catch (Throwable e) {
						ExceptionUtils.applyToUncaughtExceptionHandler(e);
					}

					subscription().cancel(LIMIT_UNSUBSCRIPTION);
				}
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == LIMIT_UNSUBSCRIPTION) {
					subscriber.onComplete(SOURCE_COMPLETED_BY_LIMIT);
				}
				else {
					subscriber.onComplete(completion);
				}
			}
		});
	}
}
