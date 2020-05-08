package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.LinkedList;
import java.util.List;

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
		buffer = new LinkedList<>();
		this.bufferCapacity = bufferCapacity;
	}

	@Override
	protected boolean onNewSubscriber(InternalSubscription subscription) {
		List<T> bufferCopy;
		publishLock.lock();
		try {
			bufferCopy = List.copyOf(buffer);
		}
		finally {
			publishLock.unlock();
		}
		for (T object : bufferCopy) {
			boolean needMore = subscription.onPublish(object);
			if (!needMore) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void prePublish(Object object, boolean isNormal) {
		if (isNormal) {
			if (buffer.size() == bufferCapacity) {
				buffer.remove(0);
			}
			buffer.add((T) object);
		}
	}

	public List<T> getBuffer() {
		publishLock.lock();
		try {
			return List.copyOf(buffer);
		}
		finally {
			publishLock.unlock();
		}
	}
}
