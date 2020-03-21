package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.CachedPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.stock.Stock;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;

public class ObjectStock<T> implements Stock<T> {

	private final CachedPublisher<T> publisher;

	public ObjectStock() {
		this.publisher = Publisher.cached();
	}

	@Override
	public final void add(T value) {
		failIfCompleted();
		publisher.publish(value);
	}

	@Override
	public final void addAll(Iterable<T> values) {
		failIfCompleted();
		for (T value : values) {
			publisher.publish(value);
		}
	}

	@Override
	public List<T> list() {
		return publisher.getCache();
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public Observable<T> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public ReadOnlyStock<T> toReadOnly() {
		return ReadOnlyObjectStock.wrap(this);
	}

	private void failIfCompleted() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}
