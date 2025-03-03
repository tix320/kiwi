package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.AbstractMutableProperty;
import com.github.tix320.kiwi.publisher.SinglePublisher;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class MapProperty<K, V> extends AbstractMutableProperty<Map<K, V>> {

	public MapProperty(Map<K, V> map) {
		super(map);
	}

	public boolean containsKey(K key) {
		return getValue().containsKey(key);
	}

	public V get(K key) {
		return getValue().get(key);
	}

	public V put(K key, V value) {
		return publisher.modifyValue(map -> {
			var prevValue = map.put(key, value);
			return new SinglePublisher.ModifyResult<>(prevValue, true);
		});
	}

	public V putIfAbsent(K key, V value) {
		return publisher.modifyValue(map -> {
			var prevValue = map.putIfAbsent(key, value);
			return new SinglePublisher.ModifyResult<>(prevValue, prevValue == null);
		});
	}

	public void putAll(Map<? extends K, ? extends V> values) {
		publisher.modifyValue(map -> {
			map.putAll(values);
			return new SinglePublisher.ModifyResult<>(null, true);
		});
	}

	public V remove(K key) {
		return publisher.modifyValue(map -> {
			var prevValue = map.remove(key);
			return new SinglePublisher.ModifyResult<>(prevValue, prevValue != null);
		});
	}

	public void clear() {
		publisher.modifyValue(map -> {
			map.clear();
			return new SinglePublisher.ModifyResult<>(null, true);
		});
	}

	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return publisher.modifyValue(map -> {
			var resultValue = map.computeIfAbsent(key, mappingFunction);
			return new SinglePublisher.ModifyResult<>(resultValue, true);
		});
	}

	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return publisher.modifyValue(map -> {
			var resultValue = map.computeIfPresent(key, remappingFunction);
			return new SinglePublisher.ModifyResult<>(resultValue, true);
		});
	}

	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return publisher.modifyValue(map -> {
			var resultValue = map.compute(key, remappingFunction);
			return new SinglePublisher.ModifyResult<>(resultValue, true);
		});
	}

	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return publisher.modifyValue(map -> {
			var resultValue = map.merge(key, value, remappingFunction);
			return new SinglePublisher.ModifyResult<>(resultValue, true);
		});
	}

	@Override
	public String toString() {
		return "MapProperty{" + getValue() + "}";
	}

}
