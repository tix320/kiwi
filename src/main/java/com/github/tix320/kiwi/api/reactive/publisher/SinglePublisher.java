package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Objects;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class SinglePublisher<T> extends BasePublisher<T> {

	private volatile T value;

	public SinglePublisher() {

	}

	public SinglePublisher(T initialValue) {
		this.value = Objects.requireNonNull(initialValue);
	}

	@Override
	protected boolean onNewSubscriber(InternalSubscription subscription) {
		if (value != null) {
			return subscription.onPublish(value);
		}
		return true;
	}

	@Override
	protected void prePublish(Object object, boolean isNormal) {
		if (isNormal) {
			value = Objects.requireNonNull((T) object);
		}
	}

	public T getValue() {
		return value;
	}
}
