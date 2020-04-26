package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class BufferPublisher<T> extends BasePublisher<T> {

	private final List<T> buffer;

	private final int bufferCapacity;

	public BufferPublisher(int bufferCapacity) {
		if (bufferCapacity < 0) {
			throw new IllegalArgumentException("Buffer size must be >=0");
		}
		buffer = new CopyOnWriteArrayList<>();
		this.bufferCapacity = bufferCapacity;
	}

	@Override
	protected void onNewSubscriber(ConditionalConsumer<T> publisherFunction) {
		for (T object : buffer) {
			boolean needMore = publisherFunction.accept(object);
			if (!needMore) {
				break;
			}
		}
	}

	@Override
	protected void prePublish(T object) {
		if (buffer.size() == bufferCapacity) {
			buffer.remove(0);
		}
		buffer.add(object);
	}

	public List<T> getBuffer() {
		return Collections.unmodifiableList(buffer);
	}
}
