package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;
import com.github.tix320.kiwi.internal.reactive.property.BaseLazyProperty;

/**
 * @author Tigran Sargsyan on 24-Mar-20.
 */
public final class SetProperty<T> extends BaseLazyProperty<Set<T>> implements Set<T> {

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
		return new UnmodifiableIterator<>(getValue().iterator());
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		getValue().forEach(action);
	}

	@Override
	public Object[] toArray() {
		return getValue().toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return getValue().toArray(a);
	}

	@Override
	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
		return getValue().toArray(generator);
	}

	@Override
	public boolean add(T t) {
		checkClosed();
		boolean added = getValue().add(t);
		if (added) {
			publishChanges();
		}
		return added;
	}

	@Override
	public boolean remove(Object o) {
		checkClosed();
		boolean removed = getValue().remove(o);
		if (removed) {
			publishChanges();
		}
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return getValue().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		checkClosed();
		boolean added = getValue().addAll(c);
		if (added) {
			publishChanges();
		}
		return added;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		checkClosed();
		boolean changed = getValue().retainAll(c);
		if (changed) {
			publishChanges();
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		checkClosed();
		boolean removed = getValue().removeAll(c);
		if (removed) {
			publishChanges();
		}
		return removed;
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		checkClosed();
		boolean removed = getValue().removeIf(filter);
		if (removed) {
			publishChanges();
		}
		return removed;
	}

	@Override
	public void clear() {
		checkClosed();
		getValue().clear();
		publishChanges();
	}

	@Override
	public Spliterator<T> spliterator() {
		return getValue().spliterator();
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
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Set)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public String toString() {
		return getValue().toString();
	}
}
