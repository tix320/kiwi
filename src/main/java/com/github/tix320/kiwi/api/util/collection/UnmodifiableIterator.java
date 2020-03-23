package com.github.tix320.kiwi.api.util.collection;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public final class UnmodifiableIterator<T> implements Iterator<T> {

	private final Iterator<T> iterator;

	public UnmodifiableIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		iterator.forEachRemaining(action);
	}
}
