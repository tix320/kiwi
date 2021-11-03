package com.github.tix320.kiwi.observable.transform.single.collect.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends Observable<R> {

	private final Observable<S> observable;

	CollectorObservable(Observable<S> observable) {
		this.observable = observable;
	}

	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		Queue<S> objects = new ConcurrentLinkedQueue<>();
		observable.subscribe(new Subscriber<>()  {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(S item) {
				objects.add(item);
			}

			@Override
			public void onComplete(Completion completion) {
				if (completion instanceof SourceCompletion) {
					try {
						subscriber.publish(collect(objects.stream()));
					}
					catch (Throwable e) {
						ExceptionUtils.applyToUncaughtExceptionHandler(e);
					}
				}
				subscriber.complete(completion);
			}
		});
	}

	protected abstract R collect(Stream<S> objects);
}
