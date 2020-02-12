package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.*;

import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class CachedPublisher<T> extends BasePublisher<T> {

	private final List<T> cache;

	public CachedPublisher() {
		this.cache = new LinkedList<>();
	}

	@Override
	protected Subscription subscribe(Subscriber<T> subscriber) {
		publishFromCache(subscriber);
		subscribers.add(subscriber);
		return () -> subscribers.remove(subscriber);
	}

	@Override
	public void publish(T object) {
		checkCompleted();
		fillCache(object);
		Iterator<Subscriber<? super T>> iterator = subscribers.iterator();
		while (iterator.hasNext()) {
			Subscriber<? super T> subscriber = iterator.next();
			try {
				boolean needMore = subscriber.consume(object);
				if (!needMore) {
					iterator.remove();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void publish(T[] objects) {
		checkCompleted();
		fillCache(objects);
		for (T object : objects) {
			publish(object);
		}
	}

	@Override
	public void publish(Iterable<T> iterable) {
		checkCompleted();
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

	private void publishFromCache(Subscriber<? super T> subscriber) {
		for (T object : cache) {
			boolean needMore = subscriber.consume(object);
			if (!needMore) {
				subscribers.remove(subscriber);
				break;
			}
		}
	}
}
