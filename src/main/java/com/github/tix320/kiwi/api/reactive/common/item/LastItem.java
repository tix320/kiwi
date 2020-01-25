package com.github.tix320.kiwi.api.reactive.common.item;

public class LastItem<T> implements Item<T> {

	private final T value;

	public LastItem(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return this.value;
	}

	@Override
	public boolean hasNext() {
		return false;
	}
}
