package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collection;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		return true;
	}

	@Override
	protected void publishOverride(Collection<InternalSubscription> subscriptions, T object) {
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
	}
}
