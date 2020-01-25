package com.github.tix320.kiwi.api.observable;

public final class PlainItem<T> implements Item<T> {

	private final T value;

	private final boolean hasNext;

	public PlainItem(T value, boolean hasNext) {
		this.value = value;
		this.hasNext = hasNext;
	}

	public T get() {
		return value;
	}

	public boolean hasNext() {
		return hasNext;
	}
}
