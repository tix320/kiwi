package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public final class ReadOnlyListProperty<T> implements ReadOnlyProperty<List<T>> {

	private final Property<List<T>> property;

	public ReadOnlyListProperty(Property<List<T>> property) {
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
	public boolean equals(Object obj) {
		return property.equals(obj);
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
