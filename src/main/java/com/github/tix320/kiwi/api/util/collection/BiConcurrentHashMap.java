package com.github.tix320.kiwi.api.util.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BiConcurrentHashMap<T1, T2> implements BiMap<T1, T2> {

	private final Map<T1, T2> straight;
	private final Map<T1, T2> straightView;

	private final Map<T2, T1> inverse;
	private final Map<T2, T1> inverseView;

	public BiConcurrentHashMap() {
		straight = new HashMap<>();
		straightView = Collections.unmodifiableMap(straight);
		inverse = new HashMap<>();
		inverseView = Collections.unmodifiableMap(inverse);
	}

	@Override
	public synchronized void put(T1 key, T2 value) {
		straight.put(key, value);
		inverse.put(value, key);
	}

	@Override
	public synchronized T2 straightRemove(T1 key) {
		T2 value = straight.remove(key);
		inverse.remove(value);
		return value;
	}

	@Override
	public T1 inverseRemove(T2 key) {
		T1 value = inverse.remove(key);
		straight.remove(value);
		return value;
	}

	@Override
	public Map<T1, T2> straightView() {
		return straightView;
	}

	@Override
	public Map<T2, T1> inverseView() {
		return inverseView;
	}
}
