package com.github.tix320.kiwi.internal.reactive.property;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ReadOnlyObjectProperty<T> implements ReadOnlyProperty<T> {

	private final Property<T> property;

	private ReadOnlyObjectProperty(Property<T> property) {
		this.property = property;
	}

	@SuppressWarnings("all")
	public static <T> ReadOnlyProperty<T> wrap(Property<T> property) {
		if (property instanceof ReadOnlyObjectProperty) {
			return (ReadOnlyObjectProperty) property;
		}
		else {
			return new ReadOnlyObjectProperty<>(property);
		}
	}

	@Override
	public T get() {
		return property.get();
	}

	@Override
	public Observable<T> asObservable() {
		return property.asObservable();
	}
}
