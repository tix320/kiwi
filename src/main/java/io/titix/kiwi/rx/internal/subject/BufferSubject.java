package io.titix.kiwi.rx.internal.subject;

import java.util.Deque;
import java.util.function.Consumer;

import io.titix.kiwi.rx.internal.observer.SourceObservable;

/**
 * @author tix32 on 21-Feb-19
 */
abstract class BufferSubject<T> extends BaseSubject<T> {

	private final Deque<T> buffer;

	private final long bufferSize;

	BufferSubject(int bufferSize) {
		buffer = buffer();
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
	public SourceObservable<T> source() {
		return new SourceObservable<>(observers, this::fillFromBuffer);
	}

	private void fillFromBuffer(Consumer<T> consumer) {
		for (T object : buffer) {
			consumer.accept(object);
		}
	}

	abstract Deque<T> buffer();
}
