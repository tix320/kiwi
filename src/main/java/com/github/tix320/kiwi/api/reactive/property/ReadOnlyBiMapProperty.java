package com.github.tix320.kiwi.api.reactive.property;

import java.util.Map;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.collection.BiMap;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableBiMap;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public final class ReadOnlyBiMapProperty<K, V> implements ReadOnlyProperty<BiMap<K, V>>, BiMap<K, V> {

	private final BiMapProperty<K, V> property;

	public ReadOnlyBiMapProperty(BiMapProperty<K, V> property) {
		this.property = property;
	}

	@Override
	public BiMap<K, V> getValue() {
		return new UnmodifiableBiMap<>(property.getValue());
	}

	@Override
	public Observable<BiMap<K, V>> asObservable() {
		return property.asObservable().map(UnmodifiableBiMap::new);
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

	@Override
	public void put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V straightRemove(K key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public K inverseRemove(V key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<K, V> straightView() {
		return property.straightView();
	}

	@Override
	public Map<V, K> inverseView() {
		return property.inverseView();
	}
}
