package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.titix.kiwi.rx.internal.observer.Observer;


/**
 * @author tix32 on 23-Feb-19
 */
public final class ConcurrentBufferSubject<T> extends BufferSubject<T> {

	public ConcurrentBufferSubject(int bufferSize) {
		super(bufferSize);
	}

	@Override
	Deque<T> buffer() {
		return new ConcurrentLinkedDeque<>();
	}

	@Override
	Collection<Observer<T>> container() {
		return new ConcurrentLinkedQueue<>();
	}
}
