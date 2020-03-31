package com.github.tix320.kiwi.api.reactive.property;

import java.util.Map;
import java.util.Objects;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.api.util.collection.BiMap;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;
import com.github.tix320.kiwi.internal.reactive.property.ReadOnlyBiMapProperty;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public class BiMapProperty<K, V> implements Property<BiMap<K, V>>, BiMap<K, V> {

	private volatile BiMap<K, V> map;

	private final SinglePublisher<BiMap<K, V>> publisher;

	public BiMapProperty() {
		this.publisher = Publisher.single();
	}

	public BiMapProperty(BiMap<K, V> initialValue) {
		this.map = Objects.requireNonNull(initialValue);
		this.publisher = Publisher.single(initialValue);
	}

	@Override
	public ReadOnlyProperty<BiMap<K, V>> toReadOnly() {
		return ReadOnlyBiMapProperty.wrap(this);
	}

	@Override
	public void setValue(BiMap<K, V> value) {
		this.map = Objects.requireNonNull(value);
		republish();
	}

	@Override
	public BiMap<K, V> getValue() {
		return map;
	}

	@Override
	public void close() {
		publisher.complete();
	}

	@Override
	public Observable<BiMap<K, V>> asObservable() {
		return publisher.asObservable();
	}

	@Override
	public void put(K key, V value) {
		failIfCompleted();
		map.put(key, value);
		republish();
	}

	@Override
	public V straightRemove(K key) {
		failIfCompleted();
		V v = map.straightRemove(key);
		republish();
		return v;
	}

	@Override
	public K inverseRemove(V key) {
		failIfCompleted();
		K k = map.inverseRemove(key);
		republish();
		return k;
	}

	@Override
	public Map<K, V> straightView() {
		return map.straightView();
	}

	@Override
	public Map<V, K> inverseView() {
		return map.inverseView();
	}

	private void republish() {
		publisher.publish(map);
	}

	private void failIfCompleted() {
		if (publisher.isCompleted()) {
			throw new PropertyClosedException("Cannot change property after close");
		}
	}
}
