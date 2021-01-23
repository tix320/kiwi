package com.github.tix320.kiwi.api.reactive.publisher;


import java.util.Iterator;
import java.util.Objects;

public class SinglePublisher<T> extends BufferedPublisher<T> {

	public SinglePublisher() {
		super(1);
	}

	public SinglePublisher(T initialValue) {
		super(1);
		synchronized (this) {
			addToQueue(initialValue);
		}
	}

	public boolean CASPublish(T expected, T newValue) {
		Objects.requireNonNull(expected);
		Objects.requireNonNull(newValue);
		synchronized (this) {
			checkCompleted();

			boolean changed = false;
			Iterator<InternalSubscription<T>> iterator;
			synchronized (this) {
				T lastItem = getItem(queueSize() - 1);
				if (lastItem.equals(expected)) {
					addToQueue(newValue);
					changed = true;
				}
				iterator = getSubscriptionsIterator();
			}

			if (changed) {
				iterator.forEachRemaining(InternalSubscription::tryPublish);
			}

			return changed;
		}
	}

	public T getValue() {
		synchronized (this) {
			if (queueSize() == 0) {
				return null;
			}

			return getItem(queueSize() - 1);
		}
	}
}
