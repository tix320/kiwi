package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Iterator;

import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {

	public synchronized void publish(T object) {
		checkCompleted();
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
	public synchronized void publish(T[] objects) {
		checkCompleted();
		for (T object : objects) {
			publish(object);
		}
	}

	@Override
	public synchronized void publish(Iterable<T> iterable) {
		checkCompleted();
		for (T object : iterable) {
			publish(object);
		}
	}

	@Override
	protected Subscription subscribe(Subscriber<T> subscriber) {
		subscribers.add(subscriber);
		return () -> subscribers.remove(subscriber);
	}
}
