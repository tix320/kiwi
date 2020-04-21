package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.tix320.kiwi.internal.reactive.property.BaseLazyProperty;

public final class MapProperty<K, V> extends BaseLazyProperty<Map<K, V>> implements Map<K, V> {

	public MapProperty() {
	}

	public MapProperty(Map<K, V> value) {
		super(value);
	}

	@Override
	public ReadOnlyMapProperty<K, V> toReadOnly() {
		return new ReadOnlyMapProperty<>(this);
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return getValue().getOrDefault(key, defaultValue);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		getValue().forEach(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		checkClosed();
		getValue().replaceAll(function);
		publishChanges();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		checkClosed();
		V v = getValue().putIfAbsent(key, value);
		publishChanges();
		return v;
	}

	@Override
	public boolean remove(Object key, Object value) {
		checkClosed();
		boolean removed = getValue().remove(key, value);
		if (removed) {
			publishChanges();
		}
		return removed;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		checkClosed();
		boolean replaced = getValue().replace(key, oldValue, newValue);
		if (replaced) {
			publishChanges();
		}
		return replaced;
	}

	@Override
	public V replace(K key, V value) {
		checkClosed();
		V v = getValue().replace(key, value);
		publishChanges();
		return v;
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		checkClosed();
		V v = getValue().computeIfAbsent(key, mappingFunction);
		publishChanges();
		return v;
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		checkClosed();
		V v = getValue().computeIfPresent(key, remappingFunction);
		publishChanges();
		return v;
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		checkClosed();
		V v = getValue().compute(key, remappingFunction);
		publishChanges();
		return v;
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		checkClosed();
		V v = getValue().merge(key, value, remappingFunction);
		publishChanges();
		return v;
	}

	@Override
	public int size() {
		return getValue().size();
	}

	@Override
	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return getValue().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return getValue().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return getValue().get(key);
	}

	@Override
	public V put(K key, V value) {
		checkClosed();
		V v = getValue().put(key, value);
		publishChanges();
		return v;
	}

	@Override
	public V remove(Object key) {
		checkClosed();
		V v = getValue().remove(key);
		publishChanges();
		return v;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		checkClosed();
		getValue().putAll(m);
		publishChanges();
	}

	@Override
	public void clear() {
		checkClosed();
		getValue().clear();
		publishChanges();
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(getValue().keySet());
	}

	@Override
	public Collection<V> values() {
		return Collections.unmodifiableCollection(getValue().values());
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(getValue().entrySet());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Map)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public String toString() {
		return getValue().toString();
	}
}
