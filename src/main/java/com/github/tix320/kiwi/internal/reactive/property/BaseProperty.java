package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseProperty<T> implements Property<T>, RepublishProperty {

	private final AtomicReference<T> value;

	private final SinglePublisher<T> publisher;

	public BaseProperty() {
		this.publisher = Publisher.single();
		this.value = new AtomicReference<>();
	}

	public BaseProperty(T value) {
		this.value = new AtomicReference<>(Objects.requireNonNull(value));
		this.publisher = Publisher.single(value);
	}

	@Override
	public synchronized final void setValue(T value) {
		checkClosed();
		this.value.set(Objects.requireNonNull(value));
		republishState();
	}

	@Override
	public synchronized final boolean compareAndSetValue(T expectedValue, T value) {
		checkClosed();
		boolean changed = this.value.compareAndSet(expectedValue, Objects.requireNonNull(value));
		republishState();
		return changed;
	}

	@Override
	public synchronized final void close() {
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
		return value.get();
	}

	@Override
	public final Observable<T> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public synchronized void republishState() {
		checkClosed();
		publisher.publish(value.get());
	}

	protected final void checkClosed() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException(String.format("%s closed. Value change is forbidden.", this));
		}
	}
}
