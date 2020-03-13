package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Map;

import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class MapProperty<K, V> extends BaseProperty<Map<K, V>> {

	public MapProperty() {
	}

	public MapProperty(Map<K, V> initialValue) {
		super(initialValue);
	}

	@Override
	public ReadOnlyProperty<Map<K, V>> toReadOnly() {
		return ReadOnlyMapProperty.wrap(this);
	}
}
