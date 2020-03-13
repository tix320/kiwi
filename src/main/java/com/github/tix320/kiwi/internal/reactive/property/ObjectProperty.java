package com.github.tix320.kiwi.internal.reactive.property;

import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ObjectProperty<T> extends BaseProperty<T> {

	public ObjectProperty() {
	}

	public ObjectProperty(T initialValue) {
		super(initialValue);
	}

	@Override
	public ReadOnlyProperty<T> toReadOnly() {
		return ReadOnlyObjectProperty.wrap(this);
	}
}

