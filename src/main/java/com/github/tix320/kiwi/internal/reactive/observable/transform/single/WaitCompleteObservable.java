package com.github.tix320.kiwi.internal.reactive.observable.transform.single;

import java.util.concurrent.CountDownLatch;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public final class WaitCompleteObservable extends TransformObservable<None> {

	private final Observable<?> observable;

	public WaitCompleteObservable(Observable<?> observable) {
		this.observable = observable;
	}

	@Override
	public void subscribe(Subscriber<? super None> subscriber) {
		CountDownLatch latch = new CountDownLatch(1);

		observable.subscribe(new Subscriber<Object>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(Object item) {
				return true;
			}

			@Override
			public boolean onError(Throwable throwable) {
				return true;
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (completionType == CompletionType.SOURCE_COMPLETED) {
					subscriber.onPublish(None.SELF);
				}
				latch.countDown();
				subscriber.onComplete(completionType);
			}
		});

		Try.runOrRethrow(latch::await);
	}
}
