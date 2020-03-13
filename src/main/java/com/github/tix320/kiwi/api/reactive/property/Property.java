package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.tix320.kiwi.internal.reactive.property.CollectionProperty;
import com.github.tix320.kiwi.internal.reactive.property.ListProperty;
import com.github.tix320.kiwi.internal.reactive.property.MapProperty;
import com.github.tix320.kiwi.internal.reactive.property.ObjectProperty;

public interface Property<T> extends ObservableProperty<T> {

	void set(T value);

	ReadOnlyProperty<T> toReadOnly();

	static <T> Property<T> forObject() {
		return new ObjectProperty<>();
	}

	static <T> Property<T> forObject(T initialValue) {
		return new ObjectProperty<>(initialValue);
	}

	static <T> Property<List<T>> forList() {
		return new ListProperty<>();
	}

	static <T> Property<List<T>> forList(List<T> initialValue) {
		return new ListProperty<>(initialValue);
	}

	static <T> Property<Collection<T>> forCollection() {
		return new CollectionProperty<>();
	}

	static <T> Property<Collection<T>> forCollection(Collection<T> initialValue) {
		return new CollectionProperty<>(initialValue);
	}

	static <K, V> Property<Map<K, V>> forMap() {
		return new MapProperty<>();
	}

	static <K, V> Property<Map<K, V>> forMap(Map<K, V> initialValue) {
		return new MapProperty<>(initialValue);
	}
}
