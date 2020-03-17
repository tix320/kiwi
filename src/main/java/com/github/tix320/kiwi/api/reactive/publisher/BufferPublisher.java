package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class BufferPublisher<T> extends BasePublisher<T> {

	private final Deque<T> buffer;

	private final int bufferCapacity;

	public BufferPublisher(int bufferCapacity) {
		buffer = new ConcurrentLinkedDeque<>();
		this.bufferCapacity = Math.max(bufferCapacity, 0);
	}

	public void publish(T object) {
		runInLock(() -> {
			failIfCompleted();
			addToBuffer(object);
			Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
			runAsync(() -> {
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
			});
		});
	}

	@Override
	public void publish(T[] objects) {
		runInLock(() -> {
			addToBuffer(objects);
			for (T object : objects) {
				publish(object);
			}
		});
	}

	@Override
	public void publish(Iterable<T> iterable) {
		runInLock(() -> {
			for (T object : iterable) {
				addToBuffer(object);
			}

			for (T object : iterable) {
				publish(object);
			}
		});
	}

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		publishFromBuffer(subscription);
		return true;
	}

	public List<T> getBuffer() {
		return new ArrayList<>(buffer);
	}

	private void addToBuffer(T object) {
		if (buffer.size() == bufferCapacity) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
	}


	private void addToBuffer(T[] objects) {
		int removeCount = Math.min(objects.length, bufferCapacity) - (bufferCapacity - buffer.size());
		for (int i = 0; i < removeCount; i++) {
			buffer.removeFirst();
		}
		int insertCount = Math.min(objects.length, bufferCapacity);
		for (int i = objects.length - insertCount; i < objects.length; i++) {
			buffer.addLast(objects[i]);
		}
	}

	private void publishFromBuffer(InternalSubscription subscription) {
		for (T object : buffer) {
			boolean needMore = subscription.onPublish(object);
			if (!needMore) {
				subscription.unsubscribe();
				break;
			}
		}
	}
}
