package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.LinkedList;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class CachedPublisher<T> extends BasePublisher<T> {

	private final List<T> cache;

	public CachedPublisher() {
		this.cache = new LinkedList<>();
	}

	public CachedPublisher(Iterable<T> iterable) {
		this.cache = new LinkedList<>();
		for (T value : iterable) {
			cache.add(value);
		}
	}

	@Override
	protected void onNewSubscriber(ConditionalConsumer<T> publisherFunction) {
		for (T object : cache) {
			boolean needMore = publisherFunction.accept(object);
			if (!needMore) {
				break;
			}
		}
	}

	@Override
	protected void prePublish(T object) {
		cache.add(object);
	}

	public List<T> getCache() {
		publishLock.lock();
		try {
			return List.copyOf(cache);
		}
		finally {
			publishLock.unlock();
		}
	}
}
