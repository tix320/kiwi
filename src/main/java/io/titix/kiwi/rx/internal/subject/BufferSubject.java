package io.titix.kiwi.rx.internal.subject;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import io.titix.kiwi.rx.internal.observer.Manager;

/**
 * @author tix32 on 21-Feb-19
 */
public final class BufferSubject<T> extends BaseSubject<T> {

	private final Deque<T> buffer;

	private final long bufferSize;

	BufferSubject(int bufferSize) {
		buffer = new ConcurrentLinkedDeque<>();
		this.bufferSize = bufferSize < 1 ? 1 : bufferSize;
	}

	@Override
	public void preNext(T object) {
		if (buffer.size() == bufferSize) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
	}

	@Override
	protected void addObserver(Consumer<T> consumer) {
		observers.add(consumer);
		nextFromBuffer(consumer);
	}

	@Override
	protected void removeObserver(Consumer<T> consumer) {
		observers.remove(consumer);
	}

	private void nextFromBuffer(Consumer<? super T> consumer) {
		buffer.forEach(consumer);
	}
}
