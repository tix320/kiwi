package com.github.tix320.kiwi.internal.reactive.property;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.FreezeableProperty;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.property.Stock;
import com.github.tix320.kiwi.api.reactive.publisher.PublisherCompletedException;
import com.github.tix320.kiwi.api.reactive.publisher.UnlimitBufferedPublisher;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseStock<T> implements Stock<T>, FreezeableProperty {

	private final UnlimitBufferedPublisher<T> publisher;

	public BaseStock() {
		this.publisher = new UnlimitBufferedPublisher<>();
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
	public abstract ReadOnlyStock<T> toReadOnly();

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
