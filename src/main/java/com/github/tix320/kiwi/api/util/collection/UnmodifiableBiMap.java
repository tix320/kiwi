package com.github.tix320.kiwi.api.util.collection;

import java.util.Map;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public final class UnmodifiableBiMap<T1, T2> implements BiMap<T1, T2> {

	private final BiMap<T1, T2> map;

	public UnmodifiableBiMap(BiMap<T1, T2> map) {
		this.map = map;
	}

	@Override
	public void put(T1 key, T2 value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T2 straightRemove(T1 key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T1 inverseRemove(T2 key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<T1, T2> straightView() {
		return map.straightView();
	}

	@Override
	public Map<T2, T1> inverseView() {
		return map.inverseView();
	}
}
