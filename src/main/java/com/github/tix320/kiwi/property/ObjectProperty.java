package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.AbstractMutableProperty;

public class ObjectProperty<T> extends AbstractMutableProperty<T> {

	public ObjectProperty() {
	}

	public ObjectProperty(T value) {
		super(value);
	}

	@Override
	public String toString() {
		return "ObjectProperty{" + getValue() + "}";
	}

}

