package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.*;

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
	public void publish(T object) {
		failIfCompleted();
		fillCache(object);
		Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
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

	@Override
	public void publish(T[] objects) {
		failIfCompleted();
		fillCache(objects);
		for (T object : objects) {
			publish(object);
		}
	}

	@Override
	public void publish(Iterable<T> iterable) {
		failIfCompleted();
		for (T object : iterable) {
			fillCache(object);
			publish(object);
		}
	}

	public List<T> getCache() {
		return Collections.unmodifiableList(cache);
	}

	private void fillCache(T object) {
		cache.add(object);
	}

	private void fillCache(T[] objects) {
		cache.addAll(Arrays.asList(objects));
	}

	private void publishFromCache(InternalSubscription subscription) {
		runAsync(() -> {
			for (T object : cache) {
				boolean needMore = subscription.onPublish(object);
				if (!needMore) {
					subscription.unsubscribe();
					break;
				}
			}
		});
	}
}
