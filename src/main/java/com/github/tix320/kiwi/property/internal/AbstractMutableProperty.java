package com.github.tix320.kiwi.property.internal;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.property.FreezeableProperty;
import com.github.tix320.kiwi.property.MutableProperty;
import com.github.tix320.kiwi.property.Property;
import com.github.tix320.kiwi.publisher.PublisherClosedException;
import com.github.tix320.kiwi.publisher.SinglePublisher;
import java.util.Objects;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class AbstractMutableProperty<T> implements MutableProperty<T>, FreezeableProperty {

	private final SinglePublisher<T> publisher;

	public AbstractMutableProperty() {
		this.publisher = new SinglePublisher<>();
	}

	public AbstractMutableProperty(T value) {
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
		return publisher.isClosed();
	}

	@Override
	public abstract Property<T> toReadOnly();

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
		} catch (PublisherClosedException e) {
			throw createClosedException();
		}
	}

	private boolean CASPublishValue(T expected, T newValue) {
		try {
			return publisher.CASPublish(expected, newValue);
		} catch (PublisherClosedException e) {
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
