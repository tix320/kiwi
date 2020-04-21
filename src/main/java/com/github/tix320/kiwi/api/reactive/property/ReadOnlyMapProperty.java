package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

public final class ReadOnlyMapProperty<K, V> implements ReadOnlyProperty<Map<K, V>>, Map<K, V> {

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

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return property.getOrDefault(key, defaultValue);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		property.forEach(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V replace(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return property.size();
	}

	@Override
	public boolean isEmpty() {
		return property.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return property.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return property.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return property.get(key);
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(property.keySet());
	}

	@Override
	public Collection<V> values() {
		return Collections.unmodifiableCollection(property.values());
	}

	@Override
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
