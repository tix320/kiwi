package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * Buffered publisher for publishing objects.
 * Any publishing for this publisher will be buffered according to configured size,
 * and subscribers may subscribe and receive objects even after publishing, which will be in buffer in that time.
 *
 * @param <T> type of objects.
 *
 * @author Tigran Sargsyan on 21-Feb-19
 */
public class BufferedPublisher<T> extends BasePublisher<T> {

	private final int bufferCapacity;

	public BufferedPublisher(int bufferCapacity) {
		super(bufferCapacity, bufferCapacity * 2);
		this.bufferCapacity = bufferCapacity;
	}

	@Override
	protected final void subscribe(InternalSubscription<T> subscription) {
		synchronized (this) {
			if (bufferCapacity < 0) {
				subscription.changeCursor(0);
			}
			else {
				subscription.changeCursor(Math.max(0, queueSize() - bufferCapacity));
			}

			subscription.tryPublish();
			if (isCompleted()) {
				subscription.complete();
			}
		}
	}

	@Override
	public final void publishOverride(T object) {
		Iterator<InternalSubscription<T>> iterator;
		boolean freeze;
		synchronized (this) {
			checkCompleted();
			addToQueue(object);
			iterator = getSubscriptionsIterator();
			freeze = isFreeze();
		}

		if (!freeze) {
			iterator.forEachRemaining(InternalSubscription::tryPublish);
		}
	}

	public final List<T> getBuffer() {
		synchronized (this) {
			int size = queueSize();
			int start = Math.max(0, size - Math.max(size, this.bufferCapacity));

			return queueSnapshot(start, size);
		}
	}
}
