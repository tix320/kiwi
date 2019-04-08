package io.titix.kiwi.rx.subject.internal;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * @author tix32 on 21-Feb-19
 */
public final class BufferSubject<T> extends BaseSubject<T> {

	private final Deque<T> buffer;

	private final long bufferSize;

	public BufferSubject(int bufferSize) {
		buffer = new ConcurrentLinkedDeque<>();
		this.bufferSize = bufferSize < 0 ? 0 : bufferSize;
	}

	@Override
	public Consumer<? super T> filterObserver(Consumer<? super T> consumer) {
		nextFromBuffer(consumer);
		return consumer;
	}

	@Override
	protected void preNext(T object) {
		if (buffer.size() == bufferSize) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
	}

	private void nextFromBuffer(Consumer<? super T> consumer) {
		buffer.forEach(consumer);
	}
}
