package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Iterator;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {

	public SimplePublisher() {
		super(0, 10);
	}

	@Override
	protected void subscribe(InternalSubscription<T> subscription) {
		synchronized (this) {
			subscription.changeCursor(queue.size());
			if (isCompleted.get()) {
				subscription.complete();
			}
		}
	}

	@Override
	public void publishOverride(T object) {
		Iterator<InternalSubscription<T>> iterator;
		synchronized (this) {
			checkCompleted();
			addToQueueWithStackTrace(object);
			iterator = getSubscriptionsIterator();
		}

		iterator.forEachRemaining(InternalSubscription::tryPublish);
	}
}
