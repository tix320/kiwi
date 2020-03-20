package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class CachedPublisher<T> extends BasePublisher<T> {

	private final List<T> cache;

	public CachedPublisher() {
		this.cache = new LinkedList<>();
	}

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		publishFromCache(subscription);
		return true;
	}

	@Override
	protected void publishOverride(Collection<InternalSubscription> subscriptions, T object) {
		addToCache(object);
		for (InternalSubscription subscription : subscriptions) {
			try {
				boolean needMore = subscription.onPublish(object);
				if (!needMore) {
					subscription.unsubscribe();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<T> getCache() {
		return Collections.unmodifiableList(cache);
	}

	private void addToCache(T object) {
		cache.add(object);
	}

	private void publishFromCache(InternalSubscription subscription) {
		for (T object : cache) {
			boolean needMore = subscription.onPublish(object);
			if (!needMore) {
				subscription.unsubscribe();
				break;
			}
		}
	}
}
