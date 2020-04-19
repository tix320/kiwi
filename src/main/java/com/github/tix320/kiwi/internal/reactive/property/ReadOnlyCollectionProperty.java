package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collection;
import java.util.Collections;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public final class ReadOnlyCollectionProperty<T> implements ReadOnlyProperty<Collection<T>> {

	private final Property<Collection<T>> property;

	public ReadOnlyCollectionProperty(Property<Collection<T>> property) {
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

	@Override
	public boolean equals(Object obj) {
		return property.equals(obj);
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
