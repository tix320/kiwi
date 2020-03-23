package com.github.tix320.kiwi.api.reactive.property;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;
import com.github.tix320.kiwi.internal.reactive.property.ReadOnlyListProperty;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableListIterator;

public class ListProperty<T> implements Property<List<T>>, List<T> {

	private volatile List<T> list;

	private final SinglePublisher<List<T>> publisher;

	public ListProperty() {
		this.publisher = Publisher.single();
	}

	public ListProperty(List<T> initialValue) {
		this.list = Objects.requireNonNull(initialValue);
		this.publisher = Publisher.single(initialValue);
	}

	@Override
	public ReadOnlyProperty<List<T>> toReadOnly() {
		return ReadOnlyListProperty.wrap(this);
	}

	@Override
	public void setValue(List<T> value) {
		failIfCompleted();
		this.list = Objects.requireNonNull(value);
		republish();
	}

	@Override
	public List<T> getValue() {
		return this;
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public Observable<List<T>> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> iterator = getValue().iterator();
		return new UnmodifiableIterator<>(iterator);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <A> A[] toArray(A[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(T t) {
		failIfCompleted();
		list.add(t);
		republish();
		return true;
	}

	@Override
	public boolean remove(Object o) {
		failIfCompleted();
		boolean removed = list.remove(o);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		failIfCompleted();
		list.addAll(c);
		republish();
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		failIfCompleted();
		list.addAll(c);
		republish();
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		failIfCompleted();
		boolean removed = list.removeAll(c);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		throw new UnsupportedOperationException("ListProperty `removeIf()` not allowed");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		failIfCompleted();
		boolean changed = list.retainAll(c);
		if (changed) {
			republish();
		}
		return changed;
	}

	@Override
	public void replaceAll(UnaryOperator<T> operator) {
		failIfCompleted();
		list.replaceAll(operator);
		republish();
	}

	@Override
	public void sort(Comparator<? super T> c) {
		failIfCompleted();
		list.sort(c);
		republish();
	}

	@Override
	public void clear() {
		failIfCompleted();
		list.clear();
		republish();
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public T set(int index, T element) {
		failIfCompleted();
		T previousElem = list.set(index, element);
		republish();
		return previousElem;
	}

	@Override
	public void add(int index, T element) {
		failIfCompleted();
		list.add(index, element);
		republish();
	}

	@Override
	public T remove(int index) {
		failIfCompleted();
		T removed = list.remove(index);
		republish();
		return removed;
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
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
		return list.spliterator();
	}

	@Override
	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
		return list.toArray(generator);
	}

	@Override
	public Stream<T> stream() {
		return list.stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return list.parallelStream();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		getValue().forEach(action);
	}

	private void republish() {
		publisher.publish(this);
	}

	private void failIfCompleted() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}
