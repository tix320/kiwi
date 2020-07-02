package com.github.tix320.kiwi.internal.reactive.property;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.property.Stock;
import com.github.tix320.kiwi.api.reactive.publisher.CachedPublisher;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseStock<T> implements Stock<T>, RepublishProperty {

	private final CachedPublisher<T> publisher;

	public BaseStock() {
		this.publisher = new CachedPublisher<>();
	}

	@Override
	public synchronized final void add(T value) {
		checkClosed();
		publish(value);
	}

	@Override
	public synchronized final void addAll(Iterable<T> values) {
		checkClosed();
		for (T value : values) {
			publish(value);
		}
	}

	@Override
	public synchronized void close() {
		publisher.complete();
	}

	@Override
	public abstract ReadOnlyStock<T> toReadOnly();

	@Override
	public final List<T> list() {
		return publisher.getBuffer();
	}

	@Override
	public final Observable<T> asObservable() {
		return publisher.asObservable();
	}

	protected synchronized void publish(T value) {
		checkClosed();
		publisher.publish(value);
	}

	protected final void checkClosed() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException(String.format("%s closed. Value change is forbidden.", this));
		}
	}
}
