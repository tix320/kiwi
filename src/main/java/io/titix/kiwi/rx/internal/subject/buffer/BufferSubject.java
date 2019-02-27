package io.titix.kiwi.rx.internal.subject.buffer;

import java.util.Deque;
import java.util.function.Consumer;

import io.titix.kiwi.rx.internal.observer.ObserverManager;
import io.titix.kiwi.rx.internal.subject.BaseSubject;

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
	public ObserverManager<T> manager() {
		return new ObserverManagerImpl();
	}

	abstract Deque<T> buffer();

	private void nextFromBuffer(Consumer<T> consumer) {
		for (T object : buffer) {
			consumer.accept(object);
		}
	}

	private final class ObserverManagerImpl implements ObserverManager<T> {

		@Override
		public void add(Consumer<T> consumer) {
			observers.add(consumer);
			nextFromBuffer(consumer);
		}

		@Override
		public void remove(Consumer<T> consumer) {
			observers.remove(consumer);
		}
	}
}
