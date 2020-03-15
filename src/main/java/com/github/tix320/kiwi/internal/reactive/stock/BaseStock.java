package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.CachedPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.stock.Stock;

public abstract class BaseStock<T> implements Stock<T> {

	protected final CachedPublisher<T> publisher;

	protected BaseStock() {
		this.publisher = Publisher.cached();
	}

	@Override
	public final void add(T value) {
		publisher.publish(value);
	}

	@Override
	public final void addAll(Iterable<T> values) {
		publisher.publish(values);
	}

	@Override
	public List<T> list() {
		return publisher.getCache();
	}

	@Override
	public Observable<T> asObservable() {
		return publisher.asObservable();
	}
}
