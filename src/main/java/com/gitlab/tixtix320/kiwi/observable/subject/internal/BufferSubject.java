package com.gitlab.tixtix320.kiwi.observable.subject.internal;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.Subscription;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class BufferSubject<T> extends BaseSubject<T> {

	private final Deque<T> buffer;

	private final long bufferSize;

	public BufferSubject(int bufferSize) {
		buffer = new ConcurrentLinkedDeque<>();
		this.bufferSize = Math.max(bufferSize, 0);
	}

	@Override
	public Subscription addObserver(Observer<? super T> observer) {
		if (completed.get()) {
			nextFromBuffer(observer);
			return () -> {};
		}
		else {
			observers.add(observer);
			nextFromBuffer(observer);
			return () -> observers.remove(observer);
		}
	}

	@Override
	protected void preNext(T object) {
		if (buffer.size() == bufferSize) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
	}

	private void nextFromBuffer(Observer<? super T> observer) {
		for (T object : buffer) {
			boolean needMore = observer.consume(object);
			if (!needMore) {
				observers.remove(observer);
				break;
			}
		}
	}
}
