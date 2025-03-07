package com.github.tix320.kiwi.observable.transform.single.collect.internal;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

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
		observable.subscribe(subscriber.fork(new MinorSubscriber<S, R>() {

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
					subscriber.publish(collect(objects.stream()));
				}
				subscriber.complete(completion);
			}
		}));
	}

	protected abstract R collect(Stream<S> objects);

}
