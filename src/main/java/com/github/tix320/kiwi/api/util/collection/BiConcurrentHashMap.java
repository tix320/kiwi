package com.github.tix320.kiwi.api.util.collection;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class BiConcurrentHashMap<T1, T2> implements BiMap<T1, T2> {

	private final Map<T1, T2> straight;
	private final Map<T1, T2> straightView;

	private final Map<T2, T1> inverse;
	private final Map<T2, T1> inverseView;

	public BiConcurrentHashMap() {
		straight = new ConcurrentHashMap<>();
		straightView = Collections.unmodifiableMap(straight);
		inverse = new ConcurrentHashMap<>();
		inverseView = Collections.unmodifiableMap(inverse);
	}

	@Override
	public synchronized void put(T1 key, T2 value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		straight.put(key, value);
		inverse.put(value, key);
	}

	@Override
	public synchronized T2 straightRemove(T1 key) {
		Objects.requireNonNull(key);
		T2 value = straight.remove(key);
		if (value == null) {
			return null;
		}
		inverse.remove(value);
		return value;
	}

	@Override
	public synchronized T1 inverseRemove(T2 key) {
		Objects.requireNonNull(key);
		T1 value = inverse.remove(key);
		if (value == null) {
			return null;
		}
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

	@Override
	public int hashCode() {
		return straight.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BiMap)) {
			return false;
		}

		BiMap<?, ?> that = (BiMap<?, ?>) obj;

		return straight.equals(that.straightView());
	}

	@Override
	public String toString() {
		return straight.toString();
	}
}
