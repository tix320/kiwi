package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collection;
import java.util.Objects;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class MonoPublisher<T> extends BasePublisher<T> {

	private volatile T value;

	@Override
	protected void onNewSubscriber(ConditionalConsumer<T> publisherFunction) {
		if (value != null) {
			publisherFunction.accept(value);
		}
	}

	@Override
	protected void prePublish( T object) {
		value = Objects.requireNonNull(object);
	}

	@Override
	protected void postPublish() {
		complete();
	}

	@Override
	public MonoObservable<T> asObservable() {
		PublisherObservable publisherObservable = new PublisherObservable();
		return publisherObservable::subscribe;
	}

	public T getValue() {
		return value;
	}
}
