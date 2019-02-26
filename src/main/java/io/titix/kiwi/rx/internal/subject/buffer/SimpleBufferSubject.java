package io.titix.kiwi.rx.internal.subject.buffer;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;


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
	protected Collection<Consumer<T>> container() {
		return new LinkedList<>();
	}
}
