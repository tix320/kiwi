package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collection;
import java.util.Collections;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ReadOnlyCollectionProperty<T> implements ReadOnlyProperty<Collection<T>> {

	private final Property<Collection<T>> property;

	private ReadOnlyCollectionProperty(Property<Collection<T>> property) {
		this.property = property;
	}

	@SuppressWarnings("all")
	public static <T> ReadOnlyProperty<Collection<T>> wrap(Property<Collection<T>> property) {
		if (property instanceof ReadOnlyCollectionProperty) {
			return (ReadOnlyCollectionProperty) property;
		}
		else {
			return new ReadOnlyCollectionProperty<>(property);
		}
	}

	@Override
	public Collection<T> getValue() {
		return Collections.unmodifiableCollection(property.getValue());
	}

	@Override
	public Observable<Collection<T>> asObservable() {
		return property.asObservable().map(Collections::unmodifiableCollection);
	}
}
