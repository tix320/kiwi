package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;
import com.github.tix320.kiwi.internal.reactive.property.ReadOnlyObjectProperty;

public class ObjectProperty<T> implements Property<T> {

	private final SinglePublisher<T> publisher;

	public ObjectProperty() {
		this.publisher = Publisher.single();
	}

	public ObjectProperty(T initialValue) {
		this.publisher = Publisher.single(initialValue);
	}

	@Override
	public void setValue(T value) {
		failIfCompleted();
		publisher.publish(value);
	}

	@Override
	public T getValue() {
		return publisher.getValue();
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public ReadOnlyProperty<T> toReadOnly() {
		return ReadOnlyObjectProperty.wrap(this);
	}

	@Override
	public Observable<T> asObservable() {
		return publisher.asObservable();
	}

	private void failIfCompleted() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}

