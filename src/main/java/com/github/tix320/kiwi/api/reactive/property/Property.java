package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.internal.reactive.property.ObjectProperty;

public interface Property<T> extends ObservableProperty<T> {

	void set(T value);

	default void reset() {
		set(get());
	}

	ReadOnlyProperty<T> toReadOnly();

	static <T> Property<T> forObject() {
		return new ObjectProperty<>();
	}

	static <T> Property<T> forObject(T initialValue) {
		return new ObjectProperty<>(initialValue);
	}
}
