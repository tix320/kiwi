package de.fsyo.uremn.collections;

import java.util.Iterator;

public final class Tuple<E> implements Iterable<E> {

	private final E[] elements;

	@SafeVarargs
	public static <E> Tuple<E> of(E... elements) {
		return new Tuple<>(elements);
	}

	public Tuple(E[] elements) {
		this.elements = elements;
	}

	public E get(int index) {
		return elements[index];
	}

	@Override
	public Iterator<E> iterator() {
		return new TupleIterator();
	}

	private final class TupleIterator implements Iterator<E> {

		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < elements.length;
		}

		@Override
		public E next() {
			return elements[index++];
		}
	}
}
