package com.github.tix320.kiwi.api.reactive.property;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;

public final class ReadOnlySetProperty<T> implements ReadOnlyProperty<Set<T>>, Set<T> {

	private final SetProperty<T> property;

	public ReadOnlySetProperty(SetProperty<T> property) {
		this.property = property;
	}

	@Override
	public Set<T> getValue() {
		return Collections.unmodifiableSet(property.getValue());
	}

	@Override
	public Observable<Set<T>> asObservable() {
		return property.asObservable().map(Collections::unmodifiableSet);
	}

	@Override
	public int hashCode() {
		return property.hashCode();
	}

	@Override
	public Spliterator<T> spliterator() {
		return property.spliterator();
	}

	@Override
	public Stream<T> stream() {
		return property.stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return property.parallelStream();
	}

	@Override
	public int size() {
		return property.size();
	}

	@Override
	public boolean isEmpty() {
		return property.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return property.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new UnmodifiableIterator<>(property.iterator());
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		property.forEach(action);
	}

	@Override
	public Object[] toArray() {
		return property.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return property.toArray(a);
	}

	@Override
	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
		return property.toArray(generator);
	}

	@Override
	public boolean add(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return property.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		return property.equals(obj);
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
