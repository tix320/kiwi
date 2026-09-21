package com.github.tix320.kiwi.observable.transform.single.collect.internal;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.skimp.exception.ExceptionUtils;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends Observable<R> {

	private final Observable<S> observable;

	CollectorObservable(Observable<S> observable) {
		this.observable = Objects.requireNonNull(observable);
	}

	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		Queue<S> objects = new ConcurrentLinkedQueue<>();
		observable.subscribe(subscriber.fork(new MinorSubscriber<S, R>() {

			@Override
			public void onSubscribe(Subscription sourceSubscription) {
				subscriber.setSubscription(new Subscription() {

					@Override
					protected void onRequest(long count) {
						sourceSubscription.requestUnbounded();
					}

					@Override
					protected void onUnboundRequest() {
						sourceSubscription.requestUnbounded();
					}

					@Override
					protected void onCancel(Unsubscription unsubscription) {
						sourceSubscription.cancel(unsubscription);
					}
				});
			}

			@Override
			public void onNext(S item) {
				objects.add(item);
			}

			@Override
			public void onComplete(Completion completion) {
				if (!(completion instanceof SourceCompletion)) {
					subscriber.complete(completion);
					return;
				}

				Exception failure = null;
				try {
					subscriber.publish(collect(objects.stream()));
				} catch (Exception exception) {
					failure = exception;
				}

				try {
					subscriber.complete(completion);
				} catch (Exception exception) {
					if (failure == null) {
						failure = exception;
					} else {
						failure.addSuppressed(exception);
					}
				}

				if (failure != null) {
					ExceptionUtils.sneakyThrow(failure);
				}
			}
		}));
	}

	protected abstract R collect(Stream<S> objects);

}
