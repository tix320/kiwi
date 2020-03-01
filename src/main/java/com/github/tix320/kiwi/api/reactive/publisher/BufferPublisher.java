package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class BufferPublisher<T> extends BasePublisher<T> {

	private final LinkedList<T> buffer;

	private final int bufferCapacity;

	public BufferPublisher(int bufferCapacity) {
		buffer = new LinkedList<>();
		this.bufferCapacity = Math.max(bufferCapacity, 0);
	}

	public synchronized void publish(T object) {
		checkCompleted();
		addToBuffer(object);
		List<InternalSubscription> subscriptions = getSubscriptions();
		for (int i = 0; i < subscriptions.size(); i++) {
			InternalSubscription subscription = subscriptions.get(i);
			try {
				boolean needMore = subscription.onPublish(object);
				if (!needMore) {
					subscription.unsubscribe();
					i--;
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
		addToBuffer(objects);
		for (T object : objects) {
			publish(object);
		}
	}

	@Override
	public synchronized void publish(Iterable<T> iterable) {
		checkCompleted();
		for (T object : iterable) {
			addToBuffer(object);
			publish(object);
		}
	}

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		publishFromBuffer(subscription);
		return true;
	}

	public List<T> getBuffer() {
		return Collections.unmodifiableList(buffer);
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
