package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import com.github.tix320.kiwi.observable.*;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
// TODO in case of user unsubscription, until subscription not deleted
public final class UntilObservable<T> implements Observable<T> {

	private static final Unsubscription UNTIL_UNSUBSCRIPTION = new Unsubscription(null);

	private final Observable<T> observable;

	private final Observable<?> until;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.until = until;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		observable.subscribe(new Subscriber<>() {

			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
				until.subscribeOnComplete(completionType -> subscription.unsubscribe(UNTIL_UNSUBSCRIPTION));
			}

			@Override
			public void onPublish(T item) {
				return subscriber.onPublish(item);
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion == UNTIL_UNSUBSCRIPTION) {
					subscriber.onComplete(SourceCompleted.DEFAULT);
				} else {
					subscriber.onComplete(completion);
				}
			}
		});
	}
}
