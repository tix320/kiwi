package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.observable.ObservableCandidate;
import com.github.tix320.skimp.collection.map.MutableBiMap;
import com.github.tix320.skimp.exception.ExceptionUtils;
import com.github.tix320.skimp.function.CheckedRunnable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	static <T> ListProperty<T> forList(List<T> initialValue) {
		return new ListProperty<>(initialValue);
	}

	static <T> SetProperty<T> forSet(Set<T> initialValue) {
		return new SetProperty<>(initialValue);
	}

	static <K, V> MapProperty<K, V> forMap(Map<K, V> initialValue) {
		return new MapProperty<>(initialValue);
	}

	static <K, V> BiMapProperty<K, V> forBiMap(MutableBiMap<K, V> initialValue) {
		return new BiMapProperty<>(initialValue);
	}

	// ---------- Atomic helper ----------

	static void updateAtomic(FreezeableProperty property, CheckedRunnable runnable) {
		updateAtomic(runnable, List.of(property));
	}

	static void updateAtomic(FreezeableProperty property1, FreezeableProperty property2, CheckedRunnable runnable) {
		updateAtomic(runnable, List.of(property1, property2));
	}

	static void updateAtomic(FreezeableProperty property1, FreezeableProperty property2, FreezeableProperty property3,
							 CheckedRunnable runnable) {
		updateAtomic(runnable, List.of(property1, property2, property3));
	}

	static void updateAtomic(Collection<FreezeableProperty> properties, CheckedRunnable runnable) {
		updateAtomic(runnable, properties);
	}

	private static void updateAtomic(CheckedRunnable runnable, Collection<FreezeableProperty> properties) {
		for (FreezeableProperty property : properties) {
			property.freeze();
		}

		Throwable ex = null;
		try {
			runnable.run();
		} catch (Throwable e) {
			ex = e;
		}

		for (FreezeableProperty property : properties) {
			property.unfreeze();
		}

		if (ex != null) {
			ExceptionUtils.sneakyThrow(ex);
		}
	}

}
