package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends MonoObservable<T> {

	private static final Unsubscription ONCE_UNSUBSCRIPTION = new Unsubscription("ONCE_UNSUBSCRIPTION");

	private static final SourceCompletion SOURCE_COMPLETED_BY_ONCE = new SourceCompletion("SOURCE_COMPLETED_BY_ONCE");

	private final Observable<T> observable;

	public OnceObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<T>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(T item) {
				try {
					subscriber.publish(item);
				}
				catch (Throwable e) {
					ExceptionUtils.applyToUncaughtExceptionHandler(e);
				}

				subscription().cancel(ONCE_UNSUBSCRIPTION);
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == ONCE_UNSUBSCRIPTION) {
					subscriber.complete(SOURCE_COMPLETED_BY_ONCE);
				}
				else {
					subscriber.complete(completion);
				}
			}
		});
	}
}
