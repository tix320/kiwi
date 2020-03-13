package com.github.tix320.kiwi.internal.reactive.property;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;

public abstract class BaseProperty<T> implements Property<T> {

	private final SinglePublisher<T> publisher;

	public BaseProperty() {
		this.publisher = Publisher.single();
	}

	public BaseProperty(T initialValue) {
		this.publisher = Publisher.single(initialValue);
	}

	@Override
	public final void set(T value) {
		publisher.publish(value);
	}

	@Override
	public final T get() {
		return publisher.getValue();
	}

	@Override
	public final Observable<T> asObservable() {
		return publisher.asObservable();
	}
}
