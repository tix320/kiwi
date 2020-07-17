package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Objects;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.FreezeableProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.reactive.publisher.PublisherCompletedException;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseProperty<T> implements Property<T>, FreezeableProperty {

	private final SinglePublisher<T> publisher;

	public BaseProperty() {
		this.publisher = new SinglePublisher<>();
	}

	public BaseProperty(T value) {
		this.publisher = new SinglePublisher<>(Objects.requireNonNull(value));
	}

	@Override
	public void setValue(T value) {
		publishValue(value);
	}

	@Override
	public boolean compareAndSetValue(T expectedValue, T value) {
		return CASPublishValue(expectedValue, value);
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public final boolean isClosed() {
		return publisher.isCompleted();
	}

	@Override
	public abstract ReadOnlyProperty<T> toReadOnly();

	@Override
	public final T getValue() {
		return publisher.getValue();
	}

	@Override
	public final Observable<T> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public final void freeze() {
		publisher.freeze();
	}

	@Override
	public final void unfreeze() {
		publisher.unfreeze();
	}

	protected final void republish() {
		publishValue(getValue());
	}

	private void publishValue(T value) {
		try {
			publisher.publish(value);
		}
		catch (PublisherCompletedException e) {
			throw createClosedException();
		}
	}

	private boolean CASPublishValue(T expected, T newValue) {
		try {
			return publisher.CASPublish(expected, newValue);
		}
		catch (PublisherCompletedException e) {
			throw createClosedException();
		}
	}

	protected final void checkClosed() {
		if (isClosed()) {
			throw createClosedException();
		}
	}

	private PropertyClosedException createClosedException() {
		return new PropertyClosedException(String.format("%s closed. Value change is forbidden.", this));
	}
}
