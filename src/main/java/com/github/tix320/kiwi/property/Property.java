package com.github.tix320.kiwi.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.tix320.kiwi.observable.ObservableCandidate;
import com.github.tix320.skimp.api.collection.BiMap;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

public interface Property<T> extends ObservableCandidate<T> {

	T getValue();

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

	static void updateAtomic(FreezeableProperty property, Runnable runnable) {
		updateAtomic(runnable, List.of(property));
	}

	static void updateAtomic(FreezeableProperty property1, FreezeableProperty property2, Runnable runnable) {
		updateAtomic(runnable, List.of(property1, property2));
	}

	static void updateAtomic(FreezeableProperty property1, FreezeableProperty property2, FreezeableProperty property3,
							 Runnable runnable) {
		updateAtomic(runnable, List.of(property1, property2, property3));
	}

	static void updateAtomic(Collection<FreezeableProperty> properties, Runnable runnable) {
		updateAtomic(runnable, properties);
	}

	private static void updateAtomic(Runnable runnable, Collection<FreezeableProperty> properties) {
		for (FreezeableProperty property : properties) {
			property.freeze();
		}

		try {
			runnable.run();
		}
		catch (Throwable e) {
			ExceptionUtils.applyToUncaughtExceptionHandler(e);
		}

		for (FreezeableProperty property : properties) {
			property.unfreeze();
		}
	}
}
