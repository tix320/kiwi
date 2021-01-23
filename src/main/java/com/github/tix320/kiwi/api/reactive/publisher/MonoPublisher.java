package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Iterator;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * Mono publisher to publish exactly one object, after which the publisher will be closed.
 * The subscribers will receive that object after subscription immediately.
 */
public final class MonoPublisher<T> extends BasePublisher<T> {

	public MonoPublisher() {
		super(1, Integer.MAX_VALUE);
	}

	@Override
	protected void subscribe(InternalSubscription<T> subscription) {
		synchronized (this) {
			subscription.changeCursor(Math.max(0, queueSize() - 1));
			subscription.tryPublish();
			if (isCompleted()) {
				subscription.complete();
			}
		}
	}

	@Override
	public void publishOverride(T object) {
		Iterator<InternalSubscription<T>> iterator;
		synchronized (this) {
			checkCompleted();
			addToQueue(object);
			iterator = getSubscriptionsIterator();
			setCompleted();
		}

		iterator.forEachRemaining(subscription -> {
			subscription.tryPublish();
			subscription.complete();
		});
	}

	@Override
	public MonoObservable<T> asObservable() {
		Observable<T> observable = super.asObservable();
		return observable::subscribe;
	}
}
