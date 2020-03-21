package com.github.tix320.kiwi.internal.reactive.property;

import java.util.ListIterator;
import java.util.function.Consumer;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public final class UnmodifiableListIterator<T> implements ListIterator<T> {

	private final ListIterator<T> listIterator;

	public UnmodifiableListIterator(ListIterator<T> listIterator) {
		this.listIterator = listIterator;
	}

	@Override
	public boolean hasNext() {
		return listIterator.hasNext();
	}

	@Override
	public T next() {
		return listIterator.next();
	}

	@Override
	public boolean hasPrevious() {
		return listIterator.hasPrevious();
	}

	@Override
	public T previous() {
		return listIterator.previous();
	}

	@Override
	public int nextIndex() {
		return listIterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return listIterator.previousIndex();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("ListProperty `iterator.remove()` not allowed");
	}

	@Override
	public void set(T t) {
		throw new UnsupportedOperationException("ListProperty `iterator.set()` not allowed");
	}

	@Override
	public void add(T t) {
		throw new UnsupportedOperationException("ListProperty `iterator.add()` not allowed");
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		listIterator.forEachRemaining(action);
	}
}
