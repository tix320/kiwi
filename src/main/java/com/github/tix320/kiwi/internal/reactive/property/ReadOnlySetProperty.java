package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collections;
import java.util.Set;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ReadOnlySetProperty<T> implements ReadOnlyProperty<Set<T>> {

	private final Property<Set<T>> property;

	private ReadOnlySetProperty(Property<Set<T>> property) {
		this.property = property;
	}

	@SuppressWarnings("all")
	public static <T> ReadOnlyProperty<Set<T>> wrap(Property<Set<T>> property) {
		if (property instanceof ReadOnlySetProperty) {
			return (ReadOnlySetProperty) property;
		}
		else {
			return new ReadOnlySetProperty<>(property);
		}
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
	public boolean equals(Object obj) {
		return property.equals(obj);
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
