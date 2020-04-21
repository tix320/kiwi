package com.github.tix320.kiwi.api.reactive.property;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableListIterator;

public final class ReadOnlyListProperty<T> implements ReadOnlyProperty<List<T>>, List<T> {

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

	@Override
	public T get(int index) {
		return property.get(index);
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		return property.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return property.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return new UnmodifiableListIterator<>(property.listIterator());
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new UnmodifiableListIterator<>(property.listIterator(index));
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
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
	public boolean addAll(int index, Collection<? extends T> c) {
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
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceAll(UnaryOperator<T> operator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sort(Comparator<? super T> c) {
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
