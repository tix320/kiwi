package com.github.tix320.kiwi.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.tix320.kiwi.property.internal.AbstractMutableProperty;
import com.github.tix320.skimp.api.collection.UnmodifiableIterator;

public final class CollectionProperty<T> extends AbstractMutableProperty<Collection<T>> {

	public CollectionProperty() {
	}

	public CollectionProperty(Collection<T> value) {
		super(value);
	}

	@Override
	public ReadOnlyCollectionProperty<T> toReadOnly() {
		return new ReadOnlyCollectionProperty<>(this);
	}

	@Override
	public synchronized void setValue(Collection<T> value) {
		super.setValue(value);
	}

	@Override
	public synchronized boolean compareAndSetValue(Collection<T> expectedValue, Collection<T> value) {
		return super.compareAndSetValue(expectedValue, value);
	}

	@Override
	public synchronized void close() {
		super.close();
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
		boolean added = getValue().add(t);
		if (added) {
			republish();
		}
		return added;
	}

	public synchronized boolean remove(T o) {
		checkClosed();
		boolean removed = getValue().remove(o);
		if (removed) {
			republish();
		}
		return removed;
	}

	public synchronized boolean addAll(Collection<? extends T> c) {
		checkClosed();
		boolean added = getValue().addAll(c);
		if (added) {
			republish();
		}
		return added;
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

	public synchronized void clear() {
		checkClosed();
		getValue().clear();
		republish();
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
		if (!(obj instanceof Collection)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public synchronized String toString() {
		return getValue().toString();
	}
}
