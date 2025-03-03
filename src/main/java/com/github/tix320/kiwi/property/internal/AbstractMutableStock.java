package com.github.tix320.kiwi.property.internal;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.property.MutableStock;
import com.github.tix320.kiwi.publisher.PublisherClosedException;
import com.github.tix320.kiwi.publisher.ReplayPublisher;
import java.util.List;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class AbstractMutableStock<T> implements MutableStock<T> {

	private final ReplayPublisher<T> publisher;

	public AbstractMutableStock(int windowSize) {
		this.publisher = new ReplayPublisher<>(windowSize);
	}

	@Override
	public final List<T> list() {
		return publisher.getBuffer();
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
	public final Observable<T> asObservable() {
		return publisher.asObservable();
	}

	private void publishValue(T value) {
		try {
			publisher.publish(value);
		} catch (PublisherClosedException e) {
			throw createClosedException();
		}
	}

	private PropertyClosedException createClosedException() {
		return new PropertyClosedException("Stock is closed. Modification is forbidden.");
	}

}
