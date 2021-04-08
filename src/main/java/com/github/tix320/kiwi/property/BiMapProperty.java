package com.github.tix320.kiwi.property;

import java.util.Map;

import com.github.tix320.kiwi.property.internal.BaseProperty;
import com.github.tix320.skimp.api.collection.BiMap;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public final class BiMapProperty<K, V> extends BaseProperty<BiMap<K, V>> {

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
	public synchronized void setValue(BiMap<K, V> value) {
		super.setValue(value);
	}

	@Override
	public synchronized boolean compareAndSetValue(BiMap<K, V> expectedValue, BiMap<K, V> value) {
		return super.compareAndSetValue(expectedValue, value);
	}

	@Override
	public synchronized void close() {
		super.close();
	}

	public synchronized void put(K key, V value) {
		checkClosed();
		getValue().put(key, value);
		republish();
	}

	public synchronized V straightRemove(K key) {
		checkClosed();
		V v = getValue().straightRemove(key);
		republish();
		return v;
	}

	public synchronized K inverseRemove(V key) {
		checkClosed();
		K k = getValue().inverseRemove(key);
		republish();
		return k;
	}

	public Map<K, V> straightView() {
		return getValue().straightView();
	}

	public Map<V, K> inverseView() {
		return getValue().inverseView();
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
		if (!(obj instanceof BiMap)) {
			return false;
		}

		return getValue().equals(obj);
	}

	@Override
	public synchronized String toString() {
		return getValue().toString();
	}
}
