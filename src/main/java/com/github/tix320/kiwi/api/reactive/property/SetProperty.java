package com.github.tix320.kiwi.api.reactive.property;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableIterator;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;
import com.github.tix320.kiwi.internal.reactive.property.ReadOnlySetProperty;

/**
 * @author Tigran Sargsyan on 24-Mar-20.
 */
public class SetProperty<T> implements Property<Set<T>>, Set<T> {

	private volatile Set<T> set;

	private final SinglePublisher<Set<T>> publisher;

	public SetProperty() {
		this.publisher = Publisher.single();
	}

	public SetProperty(Set<T> initialValue) {
		this.set = Objects.requireNonNull(initialValue);
		this.publisher = Publisher.single(initialValue);
	}

	@Override
	public ReadOnlyProperty<Set<T>> toReadOnly() {
		return ReadOnlySetProperty.wrap(this);
	}

	@Override
	public void setValue(Set<T> value) {
		failIfCompleted();
		this.set = Objects.requireNonNull(value);
		republish();
	}

	@Override
	public Set<T> getValue() {
		return set;
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public Observable<Set<T>> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new UnmodifiableIterator<>(set.iterator());
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		set.forEach(action);
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return set.toArray(a);
	}

	@Override
	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
		return set.toArray(generator);
	}

	@Override
	public boolean add(T t) {
		boolean added = set.add(t);
		if (added) {
			republish();
		}
		return added;
	}

	@Override
	public boolean remove(Object o) {
		boolean removed = set.remove(o);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean added = set.addAll(c);
		if (added) {
			republish();
		}
		return added;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = set.retainAll(c);
		if (changed) {
			republish();
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean removed = set.removeAll(c);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		boolean removed = set.removeIf(filter);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public void clear() {
		set.clear();
		republish();
	}

	@Override
	public Spliterator<T> spliterator() {
		return set.spliterator();
	}

	@Override
	public Stream<T> stream() {
		return set.stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return set.parallelStream();
	}

	private void republish() {
		publisher.publish(set);
	}

	private void failIfCompleted() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}
