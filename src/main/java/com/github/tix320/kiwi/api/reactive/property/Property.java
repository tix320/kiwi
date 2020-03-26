package com.github.tix320.kiwi.api.reactive.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Property<T> extends ObservableProperty<T> {

	void setValue(T value);

	void close();

	ReadOnlyProperty<T> toReadOnly();

	static <T> ObjectProperty<T> forObject() {
		return new ObjectProperty<>();
	}

	static <T> ObjectProperty<T> forObject(T initialValue) {
		return new ObjectProperty<>(initialValue);
	}

	static <T> ListProperty<T> forList() {
		return new ListProperty<>();
	}

	static <T> ListProperty<T> forList(List<T> initialValue) {
		return new ListProperty<>(initialValue);
	}

	static <T> SetProperty<T> forSet() {
		return new SetProperty<>();
	}

	static <T> SetProperty<T> forSet(Set<T> initialValue) {
		return new SetProperty<>(initialValue);
	}

	static <T> CollectionProperty<T> forCollection() {
		return new CollectionProperty<>();
	}

	static <T> CollectionProperty<T> forCollection(Collection<T> initialValue) {
		return new CollectionProperty<>(initialValue);
	}

	static <K, V> MapProperty<K, V> forMap() {
		return new MapProperty<>();
	}

	static <K, V> MapProperty<K, V> forMap(Map<K, V> initialValue) {
		return new MapProperty<>(initialValue);
	}
}
