package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collections;
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
		publishFromCache(publisherFunction);
	}

	@Override
	protected void prePublish(T object) {
		addToCache(object);
	}

	public List<T> getCache() {
		return Collections.unmodifiableList(cache);
	}

	private void addToCache(T object) {
		cache.add(object);
	}

	private void publishFromCache(ConditionalConsumer<T> publisherFUnction) {
		for (T object : cache) {
			boolean needMore = publisherFUnction.accept(object);
			if (!needMore) {
				break;
			}
		}
	}
}
