package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;

public final class ReadOnlyCollectionProperty<T> implements ReadOnlyProperty<Collection<T>> {

	private final CollectionProperty<T> property;

	public ReadOnlyCollectionProperty(CollectionProperty<T> property) {
		this.property = property;
	}

	@Override
	public Collection<T> getValue() {
		return Collections.unmodifiableCollection(property.getValue());
	}

	@Override
	public Observable<Collection<T>> asObservable() {
		return property.asObservable().map(Collections::unmodifiableCollection);
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

	public boolean contains(T o) {
		return property.contains(o);
	}

	public Iterator<T> iterator() {
		return new UnmodifiableIterator<>(property.iterator());
	}

	public void forEach(Consumer<? super T> action) {
		property.forEach(action);
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
