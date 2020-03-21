package com.github.tix320.kiwi.api.reactive.property;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;
import com.github.tix320.kiwi.internal.reactive.property.ReadOnlyMapProperty;

public class MapProperty<K, V> implements Property<Map<K, V>>, Map<K, V> {

	private volatile Map<K, V> map;

	private final SinglePublisher<Map<K, V>> publisher;


	public MapProperty() {
		this.publisher = Publisher.single();
	}

	public MapProperty(Map<K, V> initialValue) {
		this.map = Objects.requireNonNull(initialValue);
		this.publisher = Publisher.single(initialValue);
	}

	@Override
	public ReadOnlyProperty<Map<K, V>> toReadOnly() {
		return ReadOnlyMapProperty.wrap(this);
	}

	@Override
	public void setValue(Map<K, V> value) {
		this.map = Objects.requireNonNull(value);
		republish();
	}

	@Override
	public Map<K, V> getValue() {
		return this;
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public Observable<Map<K, V>> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		map.forEach(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		failIfCompleted();
		map.replaceAll(function);
		republish();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		failIfCompleted();
		V v = map.putIfAbsent(key, value);
		republish();
		return v;
	}

	@Override
	public boolean remove(Object key, Object value) {
		failIfCompleted();
		boolean removed = map.remove(key, value);
		if (removed) {
			republish();
		}
		return removed;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		failIfCompleted();
		boolean replaced = map.replace(key, oldValue, newValue);
		if (replaced) {
			republish();
		}
		return replaced;
	}

	@Override
	public V replace(K key, V value) {
		failIfCompleted();
		V v = map.replace(key, value);
		republish();
		return v;
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		failIfCompleted();
		V v = map.computeIfAbsent(key, mappingFunction);
		republish();
		return v;
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		failIfCompleted();
		V v = map.computeIfPresent(key, remappingFunction);
		republish();
		return v;
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		failIfCompleted();
		V v = map.compute(key, remappingFunction);
		republish();
		return v;
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		failIfCompleted();
		V v = map.merge(key, value, remappingFunction);
		republish();
		return v;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		failIfCompleted();
		V v = map.put(key, value);
		republish();
		return v;
	}

	@Override
	public V remove(Object key) {
		failIfCompleted();
		V v = map.remove(key);
		republish();
		return v;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		failIfCompleted();
		map.putAll(m);
		republish();
	}

	@Override
	public void clear() {
		failIfCompleted();
		map.clear();
		republish();
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public Collection<V> values() {
		return Collections.unmodifiableCollection(map.values());
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(map.entrySet());
	}

	private void republish() {
		publisher.publish(this);
	}

	private void failIfCompleted() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}
