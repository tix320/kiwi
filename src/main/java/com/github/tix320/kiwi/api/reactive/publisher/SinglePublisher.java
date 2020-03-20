package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collection;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class SinglePublisher<T> extends BasePublisher<T> {

	private volatile T value;

	public SinglePublisher() {

	}

	public SinglePublisher(T initialValue) {
		this.value = validateValue(initialValue);
	}

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		if (value == null) {
			return true;
		}
		else {
			return subscription.onPublish(value);
		}
	}

	@Override
	protected void publishOverride(Collection<InternalSubscription> subscriptions, T object) {
		value = validateValue(object);
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

	public T getValue() {
		return value;
	}

	private T validateValue(T value) {
		if (value == null) {
			throw new NullPointerException("Value in SinglePublisher cannot be null");
		}
		return value;
	}
}
