package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collection;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {


	public void publish(T object) {
		runInLock(() -> {
			failIfCompleted();
			Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
			for (InternalSubscription subscription : subscriptions) {
				try {
					boolean needMore = subscription.onPublish(object);
					if (!needMore) {
						subscription.unsubscribe();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void publish(T[] objects) {
		runInLock(() -> {
			for (T object : objects) {
				publish(object);
			}
		});
	}

	@Override
	public void publish(Iterable<T> iterable) {
		runInLock(() -> {
			for (T object : iterable) {
				publish(object);
			}
		});
	}

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		return true;
	}
}
