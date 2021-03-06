package com.github.tix320.kiwi.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.tix320.skimp.api.collection.BiMap;

public interface Property<T> extends ObservableProperty<T> {

	void setValue(T value);

	boolean compareAndSetValue(T expectedValue, T value);

	void close();

	boolean isClosed();

	ReadOnlyProperty<T> toReadOnly();

	// ---------- Factory methods ----------

	static <T> ObjectProperty<T> forObject() {
		return new ObjectProperty<>();
	}

	static <T> ObjectProperty<T> forObject(T initialValue) {
		return new ObjectProperty<>(initialValue);
	}

	static <T extends Enum<T>> StateProperty<T> forState(T initialValue) {
		return new StateProperty<>(initialValue);
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

	static <K, V> BiMapProperty<K, V> forBiMap() {
		return new BiMapProperty<>();
	}

	static <K, V> BiMapProperty<K, V> forBiMap(BiMap<K, V> initialValue) {
		return new BiMapProperty<>(initialValue);
	}

	// ---------- Atomic helper ----------

	static Committer updateAtomic(FreezeableProperty... properties) {
		for (FreezeableProperty property : properties) {
			property.freeze();
		}

		return () -> {
			for (FreezeableProperty property : properties) {
				property.unfreeze();
			}
		};
	}

	interface Committer {

		void commit();
	}
}
