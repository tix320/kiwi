package com.github.tix320.kiwi.api.observable;

public class RegularItem<T> implements Item<T> {

	private final T value;

	public RegularItem(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public boolean hasNext() {
		return true;
	}
}
