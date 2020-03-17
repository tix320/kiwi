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
	public void publish(T object) {
		runInLock(() -> {
			failIfCompleted();
			value = validateValue(object);
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
		throw new UnsupportedOperationException("Single publisher must publish only one value at once");
	}

	@Override
	public void publish(Iterable<T> iterable) {
		throw new UnsupportedOperationException("Single publisher must publish only one value at once");
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
