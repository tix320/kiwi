package com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.*;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> implements TransformObservable<S, R> {

	private final Observable<S> observable;

	CollectorObservable(Observable<S> observable) {
		this.observable = observable;
	}

	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		Queue<S> objects = new LinkedList<>();
		observable.subscribe(new Subscriber<S>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.onSubscribe(subscription);
			}

			@Override
			public boolean onPublish(S item) {
				objects.add(item);
				return true;
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (completionType == CompletionType.SOURCE_COMPLETED) {
					subscriber.onPublish(collect(objects.stream()));
				}
				subscriber.onComplete(completionType);
			}
		});
	}

	protected abstract R collect(Stream<S> objects);
}
