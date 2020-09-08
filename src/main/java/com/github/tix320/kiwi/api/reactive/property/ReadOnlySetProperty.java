package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.skimp.api.collection.UnmodifiableIterator;

public final class ReadOnlySetProperty<T> implements ReadOnlyProperty<Set<T>> {

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

	public Stream<T> stream() {
		return property.stream();
	}

	public int size() {
		return property.size();
	}

	public boolean isEmpty() {
		return property.isEmpty();
	}

	public boolean contains(Object o) {
		return property.contains(o);
	}

	public Iterator<T> iterator() {
		return new UnmodifiableIterator<>(property.iterator());
	}

	public void forEach(Consumer<? super T> action) {
		property.forEach(action);
	}

	public boolean containsAll(Collection<? extends T> c) {
		return property.containsAll(c);
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
