package com.github.tix320.kiwi.api.reactive.property;

import java.util.Map;

import com.github.tix320.kiwi.api.util.collection.BiMap;
import com.github.tix320.kiwi.internal.reactive.property.BaseLazyProperty;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public final class BiMapProperty<K, V> extends BaseLazyProperty<BiMap<K, V>> implements BiMap<K, V> {

	public BiMapProperty() {

	}

	public BiMapProperty(BiMap<K, V> value) {
		super(value);
	}

	@Override
	public ReadOnlyBiMapProperty<K, V> toReadOnly() {
		return new ReadOnlyBiMapProperty<>(this);
	}

	@Override
	public synchronized void put(K key, V value) {
		checkClosed();
		getValue().put(key, value);
		republishState();
	}

	@Override
	public synchronized V straightRemove(K key) {
		checkClosed();
		V v = getValue().straightRemove(key);
		republishState();
		return v;
	}

	@Override
	public synchronized K inverseRemove(V key) {
		checkClosed();
		K k = getValue().inverseRemove(key);
		republishState();
		return k;
	}

	@Override
	public Map<K, V> straightView() {
		return getValue().straightView();
	}

	@Override
	public Map<V, K> inverseView() {
		return getValue().inverseView();
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
		if (!(obj instanceof BiMap)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public String toString() {
		return getValue().toString();
	}
}
