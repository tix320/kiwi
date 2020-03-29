package com.github.tix320.kiwi.internal.reactive.observable.transform.single;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public final class WaitCompleteObservable extends TransformObservable<None> {

	private final Observable<?> observable;

	private final Duration timeout;

	public WaitCompleteObservable(Observable<?> observable, Duration timeout) {
		this.observable = observable;
		this.timeout = timeout;
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

		long millis = timeout.toMillis();
		if (millis < 0) {
			Try.runOrRethrow(latch::await);
		}
		else {
			boolean normally = Try.supplyOrRethrow(() -> latch.await(millis, TimeUnit.MILLISECONDS));
			if (!normally) {
				throw new TimeoutException(String.format("The observable not completed in %sms", millis));
			}
		}
	}
}
