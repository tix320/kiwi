package com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends TransformObservable<R> {

	private final Observable<S> observable;

	CollectorObservable(Observable<S> observable) {
		this.observable = observable;
	}

	@Override
	public Subscription subscribe(Subscriber<? super R> subscriber) {
		Queue<S> objects = new LinkedList<>();
		return observable.subscribe(new Subscriber<>() {
			@Override
			public boolean consume(S item) {
				objects.add(item);
				return true;
			}

			@Override
			public boolean onError(Throwable throwable) {
				return subscriber.onError(throwable);
			}

			@Override
			public void onComplete() {
				subscriber.consume(collect(objects.stream()));
				subscriber.onComplete();
			}
		});
	}

	protected abstract R collect(Stream<S> objects);
}
