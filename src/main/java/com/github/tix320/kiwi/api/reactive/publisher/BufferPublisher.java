package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
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
	protected void onNewSubscriber(ConditionalConsumer<T> publisherFunction) {
		publishFromBuffer(publisherFunction);
	}

	@Override
	protected void prePublish(T object) {
		addToBuffer(object);
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

	private void publishFromBuffer(ConditionalConsumer<T> publisherFunction) {
		for (T object : buffer) {
			boolean needMore = publisherFunction.accept(object);
			if (!needMore) {
				break;
			}
		}
	}
}
