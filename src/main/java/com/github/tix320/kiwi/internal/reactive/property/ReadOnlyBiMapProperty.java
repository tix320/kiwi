package com.github.tix320.kiwi.internal.reactive.property;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.util.collection.BiMap;
import com.github.tix320.kiwi.api.util.collection.UnmodifiableBiMap;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public class ReadOnlyBiMapProperty<K, V> implements ReadOnlyProperty<BiMap<K, V>> {

	private final Property<BiMap<K, V>> property;

	private ReadOnlyBiMapProperty(Property<BiMap<K, V>> property) {
		this.property = property;
	}

	@SuppressWarnings("all")
	public static <K, V> ReadOnlyProperty<BiMap<K, V>> wrap(Property<BiMap<K, V>> property) {
		if (property instanceof ReadOnlyBiMapProperty) {
			return (ReadOnlyBiMapProperty) property;
		}
		else {
			return new ReadOnlyBiMapProperty<>(property);
		}
	}

	@Override
	public BiMap<K, V> getValue() {
		return new UnmodifiableBiMap<>(property.getValue());
	}

	@Override
	public Observable<BiMap<K, V>> asObservable() {
		return property.asObservable().map(UnmodifiableBiMap::new);
	}
}
