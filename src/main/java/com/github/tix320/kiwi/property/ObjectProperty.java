package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.BaseProperty;

public class ObjectProperty<T> extends BaseProperty<T> {

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
		T value = getValue();
		Object thatValue = that.getValue();
		if (value == null) {
			return thatValue == null;
		}
		return value.equals(thatValue);
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

