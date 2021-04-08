package com.github.tix320.kiwi.publisher;

import java.util.List;

import com.github.tix320.kiwi.publisher.internal.BasePublisher;

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
	protected final int resolveInitialCursorOnSubscribe() {
		if (bufferCapacity < 0) {
			return 0;
		} else {
			return Math.max(0, queueSize() - bufferCapacity);
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
