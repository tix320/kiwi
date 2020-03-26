package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;
import com.github.tix320.kiwi.internal.reactive.property.ReadOnlyCollectionProperty;

public class CollectionProperty<T> implements Property<Collection<T>>, Collection<T> {

	private volatile Collection<T> collection;

	private final SinglePublisher<Collection<T>> publisher;

	public CollectionProperty() {
		this.publisher = Publisher.single();
	}

	public CollectionProperty(Collection<T> initialValue) {
		this.collection = Objects.requireNonNull(initialValue);
		this.publisher = Publisher.single(this);
	}

	@Override
	public void setValue(Collection<T> value) {
		failIfCompleted();
		this.collection = Objects.requireNonNull(value);
		republish();
	}

	@Override
	public Collection<T> getValue() {
		return this;
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public ReadOnlyProperty<Collection<T>> toReadOnly() {
		return ReadOnlyCollectionProperty.wrap(this);
	}

	@Override
	public Observable<Collection<T>> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return collection.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> iterator = collection.iterator();
		return new UnmodifiableIterator<>(iterator);
	}

	@Override
	public Object[] toArray() {
		return collection.toArray();
	}

	@Override
	@SuppressWarnings("all")
	public <A> A[] toArray(A[] a) {
		return collection.toArray(a);
	}

	@Override
	public boolean add(T t) {
		failIfCompleted();
		boolean added = collection.add(t);
		if (added) {
			republish();
		}
		return added;
	}

	@Override
	public boolean remove(Object o) {
		failIfCompleted();
		boolean removed = collection.remove(o);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		failIfCompleted();
		boolean added = collection.addAll(c);
		if (added) {
			republish();
		}
		return added;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		failIfCompleted();
		boolean removed = collection.removeAll(c);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		failIfCompleted();
		boolean changed = collection.retainAll(c);
		if (changed) {
			republish();
		}
		return changed;
	}

	@Override
	public void clear() {
		failIfCompleted();
		collection.clear();
		republish();
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		throw new UnsupportedOperationException("CollectionProperty `removeIf()` not allowed");
	}

	@Override
	@SuppressWarnings("all")
	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
		return collection.toArray(generator);
	}

	@Override
	public Spliterator<T> spliterator() {
		return collection.spliterator();
	}

	@Override
	public Stream<T> stream() {
		return collection.stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return collection.parallelStream();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		collection.forEach(action);
	}

	private void republish() {
		publisher.publish(collection);
	}

	private void failIfCompleted() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}
