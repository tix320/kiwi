package io.titix.kiwi.rx.internal.subject;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Subscription;

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
	public void preNext(T object) {
		if (buffer.size() == bufferSize) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
	}

	@Override
	public Subscription addObserver(Consumer<? super T> consumer) {
		observers.add(consumer);
		nextFromBuffer(consumer);
		return () -> observers.remove(consumer);
	}

	private void nextFromBuffer(Consumer<? super T> consumer) {
		buffer.forEach(consumer);
	}
}
