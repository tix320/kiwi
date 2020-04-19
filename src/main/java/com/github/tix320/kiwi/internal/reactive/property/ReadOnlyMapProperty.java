package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collections;
import java.util.Map;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public final class ReadOnlyMapProperty<K, V> implements ReadOnlyProperty<Map<K, V>> {

	private final Property<Map<K, V>> property;

	public ReadOnlyMapProperty(Property<Map<K, V>> property) {
		this.property = property;
	}

	@Override
	public Map<K, V> getValue() {
		return Collections.unmodifiableMap(property.getValue());
	}

	@Override
	public Observable<Map<K, V>> asObservable() {
		return property.asObservable().map(Collections::unmodifiableMap);
	}

	@Override
	public int hashCode() {
		return property.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return property.equals(obj);
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
