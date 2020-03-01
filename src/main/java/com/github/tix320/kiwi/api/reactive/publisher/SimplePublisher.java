package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.List;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {

	public synchronized void publish(T object) {
		checkCompleted();
		List<InternalSubscription> subscriptions = getSubscriptions();
		for (int i = 0; i < subscriptions.size(); i++) {
			InternalSubscription subscription = subscriptions.get(i);
			try {
				boolean needMore = subscription.onPublish(object);
				if (!needMore) {
					subscription.unsubscribe();
					i--;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void publish(T[] objects) {
		checkCompleted();
		for (T object : objects) {
			publish(object);
		}
	}

	@Override
	public synchronized void publish(Iterable<T> iterable) {
		checkCompleted();
		for (T object : iterable) {
			publish(object);
		}
	}

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		return true;
	}
}
