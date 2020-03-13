package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collection;

import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class CollectionProperty<T> extends BaseProperty<Collection<T>> {

	public CollectionProperty() {
	}

	public CollectionProperty(Collection<T> initialValue) {
		super(initialValue);
	}

	@Override
	public ReadOnlyProperty<Collection<T>> toReadOnly() {
		return ReadOnlyCollectionProperty.wrap(this);
	}
}
