package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.internal.reactive.property.BaseLazyProperty;

public final class ObjectProperty<T> extends BaseLazyProperty<T> {

	public ObjectProperty() {
	}

	public ObjectProperty(T value) {
		super(value);
	}

	@Override
	public ReadOnlyObjectProperty<T> toReadOnly() {
		return new ReadOnlyObjectProperty<>(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ObjectProperty<?> that = (ObjectProperty<?>) o;
		if (getValue() == null) {
			return that.getValue() == null;
		}
		return getValue().equals(that.getValue());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public String toString() {
		return "ObjectProperty: " + getValue().toString();
	}
}

