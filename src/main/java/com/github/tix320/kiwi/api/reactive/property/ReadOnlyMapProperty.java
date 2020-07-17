package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

public final class ReadOnlyMapProperty<K, V> implements ReadOnlyProperty<Map<K, V>> {

	private final MapProperty<K, V> property;

	public ReadOnlyMapProperty(MapProperty<K, V> property) {
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

	public V getOrDefault(K key, V defaultValue) {
		return property.getOrDefault(key, defaultValue);
	}

	public void forEach(BiConsumer<? super K, ? super V> action) {
		property.forEach(action);
	}

	public boolean isEmpty() {
		return property.isEmpty();
	}

	public boolean containsKey(K key) {
		return property.containsKey(key);
	}

	public boolean containsValue(V value) {
		return property.containsValue(value);
	}

	public V get(K key) {
		return property.get(key);
	}

	public Set<K> keySet() {
		return Collections.unmodifiableSet(property.keySet());
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(property.values());
	}

	public Set<Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(property.entrySet());
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
