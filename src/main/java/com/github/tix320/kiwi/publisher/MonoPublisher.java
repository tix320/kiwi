package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;

/**
 * Mono publisher to publish exactly one object, after which the publisher will be closed.
 * The subscribers will receive that object after subscription immediately.
 */
public final class MonoPublisher<T> extends BasePublisher<T> {

	public MonoPublisher() {
		super(1, Integer.MAX_VALUE);
	}

	@Override
	protected int resolveInitialCursorOnSubscribe() {
		return Math.max(0, queueSize() - 1);
	}

	@Override
	protected void postPublish() {
		complete();
	}

	@Override
	public MonoObservable<T> asObservable() {
		Observable<T> observable = super.asObservable();
		return new MonoObservable<>() {
			@Override
			public void subscribe(Subscriber<? super T> subscriber) {
				observable.subscribe(subscriber);
			}
		};
	}
}
