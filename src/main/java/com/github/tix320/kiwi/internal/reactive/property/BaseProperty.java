package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.reactive.publisher.PublisherCompletedException;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseProperty<T> implements Property<T>, RepublishProperty {

	private final SinglePublisher<T> publisher;

	private final AtomicReference<State> state;

	public BaseProperty() {
		this.publisher = new SinglePublisher<>();
		this.state = new AtomicReference<>(new State(null));
	}

	public BaseProperty(T value) {
		this.publisher = new SinglePublisher<>(Objects.requireNonNull(value));
		this.state = new AtomicReference<>(new State(null));
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
	public void republishState() {
		if (!PropertyAtomicContext.inAtomicContext(this)) {
			publishValue(publisher.getValue());
		}
	}

	protected void doAtomic(Runnable runnable){

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

	private static final class State {
		private final Thread changer;

		public State(Thread changer) {
			this.changer = changer;
		}

		public Thread getChanger() {
			return changer;
		}
	}
}
