package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collections;
import java.util.Map;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ReadOnlyMapProperty<K, V> implements ReadOnlyProperty<Map<K, V>> {

	private final Property<Map<K, V>> property;

	private ReadOnlyMapProperty(Property<Map<K, V>> property) {
		this.property = property;
	}

	@SuppressWarnings("all")
	public static <K, V> ReadOnlyProperty<Map<K, V>> wrap(Property<Map<K, V>> property) {
		if (property instanceof ReadOnlyMapProperty) {
			return (ReadOnlyMapProperty) property;
		}
		else {
			return new ReadOnlyMapProperty<>(property);
		}
	}

	@Override
	public Map<K, V> getValue() {
		return Collections.unmodifiableMap(property.getValue());
	}

	@Override
	public Observable<Map<K, V>> asObservable() {
		return property.asObservable().map(Collections::unmodifiableMap);
	}
}
