package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> implements MonoObservable<T> {

	private static final RegularUnsubscription ONCE_UNSUBSCRIPTION = new RegularUnsubscription(null);

	private final Observable<T> observable;

	public OnceObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public void onPublish(T item) {
				try {
					final RegularUnsubscription regularUnsubscription = subscriber.onPublish(item);
					if (regularUnsubscription != null) {
						return regularUnsubscription;
					}
				} catch (Throwable e) {
					ExceptionUtils.applyToUncaughtExceptionHandler(e);
				}

				return ONCE_UNSUBSCRIPTION;
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == ONCE_UNSUBSCRIPTION) {
					subscriber.onComplete(SourceCompleted.DEFAULT);
				} else {
					subscriber.onComplete(completion);
				}
			}
		});
	}
}
