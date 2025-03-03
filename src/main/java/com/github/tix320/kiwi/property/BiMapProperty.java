package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.AbstractMutableProperty;
import com.github.tix320.kiwi.publisher.SinglePublisher.ModifyResult;
import com.github.tix320.skimp.collection.map.MutableBiMap;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public final class BiMapProperty<K, V> extends AbstractMutableProperty<MutableBiMap<K, V>> {

	public BiMapProperty(MutableBiMap<K, V> map) {
		super(map);
	}

	public void put(K key, V value) {
		publisher.modifyValue(map -> {
			map.put(key, value);
			return new ModifyResult<>(null, true);
		});
	}

	public V remove(K key) {
		return publisher.modifyValue(map -> {
			var prevValue = map.remove(key);
			return new ModifyResult<>(prevValue, prevValue != null);
		});
	}

	public K removeInverse(V key) {
		return publisher.modifyValue(map -> {
			var prevValue = map.removeInverse(key);
			return new ModifyResult<>(prevValue, prevValue != null);
		});
	}

	@Override
	public String toString() {
		return "BiMapProperty{" + getValue() + "}";
	}

}
