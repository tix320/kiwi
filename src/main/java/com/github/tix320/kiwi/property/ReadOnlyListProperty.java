package com.github.tix320.kiwi.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.skimp.api.collection.UnmodifiableIterator;

public final class ReadOnlyListProperty<T> implements ReadOnlyProperty<List<T>> {

	private final ListProperty<T> property;

	public ReadOnlyListProperty(ListProperty<T> property) {
		this.property = property;
	}

	@Override
	public List<T> getValue() {
		return Collections.unmodifiableList(property.getValue());
	}

	@Override
	public Observable<List<T>> asObservable() {
		return property.asObservable().map(Collections::unmodifiableList);
	}

	@Override
	public int hashCode() {
		return property.hashCode();
	}

	public T get(int index) {
		return property.get(index);
	}

	public int indexOf(T o) {
		return property.indexOf(o);
	}

	public int lastIndexOf(T o) {
		return property.lastIndexOf(o);
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
