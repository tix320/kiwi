package com.github.tix320.kiwi.api.reactive.property;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableListIterator;
import com.github.tix320.kiwi.internal.reactive.property.BaseLazyProperty;

public final class ListProperty<T> extends BaseLazyProperty<List<T>> implements List<T> {

	public ListProperty() {
	}

	public ListProperty(List<T> value) {
		super(value);
	}

	@Override
	public ReadOnlyListProperty<T> toReadOnly() {
		return new ReadOnlyListProperty<>(this);
	}

	@Override
	public int size() {
		return getValue().size();
	}

	@Override
	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return getValue().contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> iterator = getValue().iterator();
		return new UnmodifiableIterator<>(iterator);
	}

	@Override
	public Object[] toArray() {
		return getValue().toArray();
	}

	@Override
	public <A> A[] toArray(A[] a) {
		return getValue().toArray(a);
	}

	@Override
	public synchronized boolean add(T t) {
		checkClosed();
		getValue().add(t);
		republishState();
		return true;
	}

	@Override
	public synchronized boolean remove(Object o) {
		checkClosed();
		boolean removed = getValue().remove(o);
		if (removed) {
			republishState();
		}
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return getValue().containsAll(c);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> c) {
		checkClosed();
		getValue().addAll(c);
		republishState();
		return true;
	}

	@Override
	public synchronized boolean addAll(int index, Collection<? extends T> c) {
		checkClosed();
		getValue().addAll(c);
		republishState();
		return true;
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c) {
		checkClosed();
		boolean removed = getValue().removeAll(c);
		if (removed) {
			republishState();
		}
		return removed;
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		throw new UnsupportedOperationException("ListProperty `removeIf()` not allowed");
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		checkClosed();
		boolean changed = getValue().retainAll(c);
		if (changed) {
			republishState();
		}
		return changed;
	}

	@Override
	public synchronized void replaceAll(UnaryOperator<T> operator) {
		checkClosed();
		getValue().replaceAll(operator);
		republishState();
	}

	@Override
	public synchronized void sort(Comparator<? super T> c) {
		checkClosed();
		getValue().sort(c);
		republishState();
	}

	@Override
	public synchronized void clear() {
		checkClosed();
		getValue().clear();
		republishState();
	}

	@Override
	public T get(int index) {
		return getValue().get(index);
	}

	@Override
	public synchronized T set(int index, T element) {
		checkClosed();
		T previousElem = getValue().set(index, element);
		republishState();
		return previousElem;
	}

	@Override
	public synchronized void add(int index, T element) {
		checkClosed();
		getValue().add(index, element);
		republishState();
	}

	@Override
	public synchronized T remove(int index) {
		checkClosed();
		T removed = getValue().remove(index);
		republishState();
		return removed;
	}

	@Override
	public int indexOf(Object o) {
		return getValue().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return getValue().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		ListIterator<T> listIterator = getValue().listIterator();
		return new UnmodifiableListIterator<>(listIterator);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		ListIterator<T> listIterator = getValue().listIterator(index);
		return new UnmodifiableListIterator<>(listIterator);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("ListProperty `subList()` not allowed");
	}

	@Override
	public Spliterator<T> spliterator() {
		return getValue().spliterator();
	}

	@Override
	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
		return getValue().toArray(generator);
	}

	@Override
	public Stream<T> stream() {
		return getValue().stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return getValue().parallelStream();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		getValue().forEach(action);
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof List)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public String toString() {
		return getValue().toString();
	}
}
