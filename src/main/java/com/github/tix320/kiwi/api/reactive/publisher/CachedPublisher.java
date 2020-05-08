package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.LinkedList;
import java.util.List;

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
	protected boolean onNewSubscriber(InternalSubscription subscription) {
		List<T> bufferCopy;
		publishLock.lock();
		try {
			bufferCopy = List.copyOf(cache);
		}
		finally {
			publishLock.unlock();
		}
		for (T object : bufferCopy) {
			boolean needMore = subscription.onPublish(object);
			if (!needMore) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void prePublish(Object object, boolean isNormal) {
		if (isNormal) {
			cache.add((T) object);
		}
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
