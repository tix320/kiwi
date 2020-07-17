package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.tix320.kiwi.internal.reactive.property.BaseProperty;

public final class MapProperty<K, V> extends BaseProperty<Map<K, V>> {

	public MapProperty() {
	}

	public MapProperty(Map<K, V> value) {
		super(new ConcurrentHashMap<>(value));
	}

	@Override
	public ReadOnlyMapProperty<K, V> toReadOnly() {
		return new ReadOnlyMapProperty<>(this);
	}

	@Override
	public synchronized void setValue(Map<K, V> value) {
		super.setValue(new ConcurrentHashMap<>(value));
	}

	@Override
	public synchronized boolean compareAndSetValue(Map<K, V> expectedValue, Map<K, V> value) {
		return super.compareAndSetValue(expectedValue, new ConcurrentHashMap<>(value));
	}

	@Override
	public synchronized void close() {
		super.close();
	}

	public synchronized V getOrDefault(K key, V defaultValue) {
		return getValue().getOrDefault(key, defaultValue);
	}

	public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
		getValue().forEach(action);
	}

	public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		checkClosed();
		getValue().replaceAll(function);
		republish();
	}

	public synchronized V putIfAbsent(K key, V value) {
		checkClosed();
		V v = getValue().putIfAbsent(key, value);
		republish();
		return v;
	}

	public synchronized boolean remove(K key, V value) {
		checkClosed();
		boolean removed = getValue().remove(key, value);
		if (removed) {
			republish();
		}
		return removed;
	}

	public synchronized boolean replace(K key, V oldValue, V newValue) {
		checkClosed();
		boolean replaced = getValue().replace(key, oldValue, newValue);
		if (replaced) {
			republish();
		}
		return replaced;
	}

	public synchronized V replace(K key, V value) {
		checkClosed();
		V v = getValue().replace(key, value);
		republish();
		return v;
	}

	public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		checkClosed();
		V v = getValue().computeIfAbsent(key, mappingFunction);
		republish();
		return v;
	}

	public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		checkClosed();
		V v = getValue().computeIfPresent(key, remappingFunction);
		republish();
		return v;
	}

	public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		checkClosed();
		V v = getValue().compute(key, remappingFunction);
		republish();
		return v;
	}

	public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		checkClosed();
		V v = getValue().merge(key, value, remappingFunction);
		republish();
		return v;
	}

	public synchronized boolean isEmpty() {
		return getValue().isEmpty();
	}

	public synchronized boolean containsKey(K key) {
		return getValue().containsKey(key);
	}

	public synchronized boolean containsValue(V value) {
		return getValue().containsValue(value);
	}

	public synchronized V get(K key) {
		return getValue().get(key);
	}

	public synchronized V put(K key, V value) {
		checkClosed();
		V v = getValue().put(key, value);
		republish();
		return v;
	}

	public synchronized V remove(K key) {
		checkClosed();
		V v = getValue().remove(key);
		republish();
		return v;
	}

	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		checkClosed();
		getValue().putAll(m);
		republish();
	}

	public synchronized void clear() {
		checkClosed();
		getValue().clear();
		republish();
	}

	public synchronized Set<K> keySet() {
		return Collections.unmodifiableSet(getValue().keySet());
	}

	public synchronized Collection<V> values() {
		return Collections.unmodifiableCollection(getValue().values());
	}

	public synchronized Set<Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(getValue().entrySet());
	}

	@Override
	public synchronized int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Map)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public synchronized String toString() {
		return getValue().toString();
	}
}
