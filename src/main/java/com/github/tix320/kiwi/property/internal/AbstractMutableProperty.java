package com.github.tix320.kiwi.property.internal;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.property.FreezeableProperty;
import com.github.tix320.kiwi.property.MutableProperty;
import com.github.tix320.kiwi.publisher.PublisherClosedException;
import com.github.tix320.kiwi.publisher.SinglePublisher;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class AbstractMutableProperty<T> implements MutableProperty<T>, FreezeableProperty {

	protected final SinglePublisher<T> publisher;

	public AbstractMutableProperty() {
		this.publisher = new SinglePublisher<>();
	}

	public AbstractMutableProperty(T value) {
		this.publisher = new SinglePublisher<>(value);
	}

	@Override
	public final T getValue() {
		return publisher.getValue();
	}

	@Override
	public final void setValue(T value) {
		publishValue(value);
	}

	@Override
	public final boolean compareAndSetValue(T expectedValue, T value) {
		return CASPublishValue(expectedValue, value);
	}

	@Override
	public final void close() {
		publisher.complete();
	}

	@Override
	public final boolean isClosed() {
		return publisher.isClosed();
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

	private PropertyClosedException createClosedException() {
		return new PropertyClosedException("Property is closed. Modification is forbidden.");
	}

}
