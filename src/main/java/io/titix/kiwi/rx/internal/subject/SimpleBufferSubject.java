package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import io.titix.kiwi.rx.internal.observer.Observer;


/**
 * @author tix32 on 23-Feb-19
 */
public final class SimpleBufferSubject<T> extends BufferSubject<T> {

	public SimpleBufferSubject(int bufferSize) {
		super(bufferSize);
	}

	@Override
	Deque<T> buffer() {
		return new LinkedList<>();
	}

	@Override
	Collection<Observer<T>> container() {
		return new LinkedList<>();
	}
}
