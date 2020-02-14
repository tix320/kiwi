package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Iterator;

import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class SinglePublisher<T> extends BasePublisher<T> {

	private T value;

	public SinglePublisher(T initialValue) {
		this.value = initialValue;
	}

	@Override
	protected Subscription subscribe(Subscriber<T> subscriber) {
		boolean needMore = subscriber.consume(value);
		if (!needMore) {
			return () -> {};
		}
		subscribers.add(subscriber);
		return () -> subscribers.remove(subscriber);
	}

	@Override
	public synchronized void publish(T object) {
		value = object;
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
		throw new UnsupportedOperationException("Single publisher must publish only one value at once");
	}

	@Override
	public synchronized void publish(Iterable<T> iterable) {
		throw new UnsupportedOperationException("Single publisher must publish only one value at once");
	}

	public T getValue() {
		return value;
	}
}