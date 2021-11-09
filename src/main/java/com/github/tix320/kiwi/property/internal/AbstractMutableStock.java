package com.github.tix320.kiwi.property.internal;

import java.util.List;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.property.FreezeableProperty;
import com.github.tix320.kiwi.property.MutableStock;
import com.github.tix320.kiwi.property.Stock;
import com.github.tix320.kiwi.publisher.BufferedPublisher;
import com.github.tix320.kiwi.publisher.PublisherCompletedException;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class AbstractMutableStock<T> implements MutableStock<T>, FreezeableProperty {

	private final BufferedPublisher<T> publisher;

	public AbstractMutableStock() {
		this.publisher = new BufferedPublisher<>(Integer.MAX_VALUE);
	}

	@Override
	public final void add(T value) {
		publishValue(value);
	}

	@Override
	public final void close() {
		publisher.complete();
	}

	@Override
	public abstract Stock<T> toReadOnly();

	@Override
	public final List<T> list() {
		return publisher.getBuffer();
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
		}
		catch (PublisherCompletedException e) {
			throw createClosedException();
		}
	}

	private PropertyClosedException createClosedException() {
		return new PropertyClosedException(String.format("%s closed. Value adding is forbidden.", this));
	}
}
