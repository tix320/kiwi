package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;
import com.github.tix320.kiwi.internal.reactive.property.BaseProperty;

/**
 * @author Tigran Sargsyan on 24-Mar-20.
 */
public final class SetProperty<T> extends BaseProperty<Set<T>> {

	public SetProperty() {
	}

	public SetProperty(Set<T> value) {
		super(value);
	}

	@Override
	public ReadOnlyProperty<Set<T>> toReadOnly() {
		return new ReadOnlySetProperty<>(this);
	}

	@Override
	public synchronized void setValue(Set<T> value) {
		super.setValue(value);
	}

	@Override
	public synchronized boolean compareAndSetValue(Set<T> expectedValue, Set<T> value) {
		return super.compareAndSetValue(expectedValue, value);
	}

	@Override
	public synchronized void close() {
		super.close();
	}

	@Override
	public synchronized void republishState() {
		super.republishState();
	}

	public synchronized int size() {
		return getValue().size();
	}

	public synchronized boolean isEmpty() {
		return getValue().isEmpty();
	}

	public synchronized boolean contains(Object o) {
		return getValue().contains(o);
	}

	public synchronized Iterator<T> iterator() {
		return new UnmodifiableIterator<>(getValue().iterator());
	}

	public synchronized void forEach(Consumer<? super T> action) {
		getValue().forEach(action);
	}

	public synchronized boolean add(T t) {
		checkClosed();
		boolean added = getValue().add(t);
		if (added) {
			republishState();
		}
		return added;
	}

	public synchronized boolean remove(T o) {
		checkClosed();
		boolean removed = getValue().remove(o);
		if (removed) {
			republishState();
		}
		return removed;
	}

	public synchronized boolean containsAll(Collection<? extends T> c) {
		return getValue().containsAll(c);
	}

	public synchronized boolean addAll(Collection<? extends T> c) {
		checkClosed();
		boolean added = getValue().addAll(c);
		if (added) {
			republishState();
		}
		return added;
	}

	public synchronized boolean retainAll(Collection<? extends T> c) {
		checkClosed();
		boolean changed = getValue().retainAll(c);
		if (changed) {
			republishState();
		}
		return changed;
	}

	public synchronized boolean removeAll(Collection<? extends T> c) {
		checkClosed();
		boolean removed = getValue().removeAll(c);
		if (removed) {
			republishState();
		}
		return removed;
	}

	public synchronized void clear() {
		checkClosed();
		getValue().clear();
		republishState();
	}

	public Stream<T> stream() {
		return getValue().stream();
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
		if (!(obj instanceof Set)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public synchronized String toString() {
		return getValue().toString();
	}
}
