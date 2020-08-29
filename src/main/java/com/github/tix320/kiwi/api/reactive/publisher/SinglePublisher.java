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
			addToQueueWithStackTrace(initialValue);
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
				T lastItem = queue.get(queue.size() - 1).getValue();
				if (lastItem.equals(expected)) {
					addToQueueWithStackTrace(newValue);
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
			if (queue.isEmpty()) {
				return null;
			}

			return queue.get(queue.size() - 1).getValue();
		}
	}
}
