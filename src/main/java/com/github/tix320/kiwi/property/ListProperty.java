package com.github.tix320.kiwi.property;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.github.tix320.kiwi.property.internal.BaseProperty;
import com.github.tix320.skimp.api.collection.UnmodifiableIterator;

public final class ListProperty<T> extends BaseProperty<List<T>> {

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
	public synchronized void setValue(List<T> value) {
		super.setValue(value);
	}

	@Override
	public synchronized boolean compareAndSetValue(List<T> expectedValue, List<T> value) {
		return super.compareAndSetValue(expectedValue, value);
	}

	@Override
	public synchronized void close() {
		super.close();
	}

	public synchronized int size() {
		return getValue().size();
	}

	public synchronized boolean isEmpty() {
		return getValue().isEmpty();
	}

	public synchronized boolean contains(T o) {
		return getValue().contains(o);
	}

	public synchronized Iterator<T> iterator() {
		Iterator<T> iterator = getValue().iterator();
		return new UnmodifiableIterator<>(iterator);
	}

	public synchronized boolean add(T t) {
		checkClosed();
		getValue().add(t);
		republish();
		return true;
	}

	public synchronized boolean remove(T o) {
		checkClosed();
		boolean removed = getValue().remove(o);
		if (removed) {
			republish();
		}
		return removed;
	}

	public synchronized boolean containsAll(Collection<? extends T> c) {
		return getValue().containsAll(c);
	}

	public synchronized boolean addAll(Collection<? extends T> c) {
		checkClosed();
		getValue().addAll(c);
		republish();
		return true;
	}

	public synchronized boolean addAll(int index, Collection<? extends T> c) {
		checkClosed();
		getValue().addAll(c);
		republish();
		return true;
	}

	public synchronized boolean removeAll(Collection<? extends T> c) {
		checkClosed();
		boolean removed = getValue().removeAll(c);
		if (removed) {
			republish();
		}
		return removed;
	}

	public synchronized boolean retainAll(Collection<? extends T> c) {
		checkClosed();
		boolean changed = getValue().retainAll(c);
		if (changed) {
			republish();
		}
		return changed;
	}

	public synchronized void replaceAll(UnaryOperator<T> operator) {
		checkClosed();
		getValue().replaceAll(operator);
		republish();
	}

	public synchronized void sort(Comparator<? super T> c) {
		checkClosed();
		getValue().sort(c);
		republish();
	}

	public synchronized void clear() {
		checkClosed();
		getValue().clear();
		republish();
	}

	public synchronized T get(int index) {
		return getValue().get(index);
	}

	public synchronized T set(int index, T element) {
		checkClosed();
		T previousElem = getValue().set(index, element);
		republish();
		return previousElem;
	}

	public synchronized void add(int index, T element) {
		checkClosed();
		getValue().add(index, element);
		republish();
	}

	public synchronized T remove(int index) {
		checkClosed();
		T removed = getValue().remove(index);
		republish();
		return removed;
	}

	public synchronized int indexOf(T o) {
		return getValue().indexOf(o);
	}

	public synchronized int lastIndexOf(T o) {
		return getValue().lastIndexOf(o);
	}

	public synchronized Stream<T> stream() {
		return getValue().stream();
	}

	public synchronized void forEach(Consumer<? super T> action) {
		getValue().forEach(action);
	}

	@Override
	public synchronized int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof List)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public synchronized String toString() {
		return getValue().toString();
	}
}
