package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Objects;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class SinglePublisher<T> extends BasePublisher<T> {

	private volatile T value;

	public SinglePublisher() {

	}

	public SinglePublisher(T initialValue) {
		this.value = Objects.requireNonNull(initialValue);
	}

	@Override
	protected void onNewSubscriber(ConditionalConsumer<T> publisherFunction) {
		if (value != null) {
			publisherFunction.accept(value);
		}
	}

	@Override
	protected void prePublish(T object) {
		value = Objects.requireNonNull(object);
	}

	public T getValue() {
		return value;
	}
}
