package com.github.tix320.kiwi.internal.reactive.property;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ListProperty<T> extends BaseProperty<List<T>> {

	public ListProperty() {
	}

	public ListProperty(List<T> initialValue) {
		super(initialValue);
	}

	@Override
	public ReadOnlyProperty<List<T>> toReadOnly() {
		return ReadOnlyListProperty.wrap(this);
	}
}
