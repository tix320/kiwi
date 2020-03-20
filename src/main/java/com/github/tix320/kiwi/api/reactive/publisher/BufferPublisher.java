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

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		publishFromBuffer(subscription);
		return true;
	}

	@Override
	protected void publishOverride(Collection<InternalSubscription> subscriptions, T object) {
		addToBuffer(object);
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

	public List<T> getBuffer() {
		return new ArrayList<>(buffer);
	}

	private void addToBuffer(T object) {
		if (buffer.size() == bufferCapacity) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
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
