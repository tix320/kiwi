package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Objects;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseProperty<T> implements Property<T> {

	private volatile T value;

	private final SinglePublisher<T> publisher;

	public BaseProperty() {
		this.publisher = Publisher.single();
	}

	public BaseProperty(T value) {
		this.value = Objects.requireNonNull(value);
		this.publisher = Publisher.single();
	}

	@Override
	public final void setValue(T value) {
		checkClosed();
		this.value = Objects.requireNonNull(value);
		publishChanges();
	}

	@Override
	public final void close() {
		publisher.complete();
	}

	@Override
	public boolean isClosed() {
		return publisher.isCompleted();
	}

	@Override
	public abstract ReadOnlyProperty<T> toReadOnly();

	@Override
	public final T getValue() {
		return value;
	}

	@Override
	public final Observable<T> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public void publishChanges() {
		publisher.publish(value);
	}

	protected final void checkClosed() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}
