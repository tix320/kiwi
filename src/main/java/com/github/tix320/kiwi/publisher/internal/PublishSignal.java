package com.github.tix320.kiwi.publisher.internal;

import com.github.tix320.kiwi.publisher.Signal;

public final class PublishSignal<T> extends Signal {

	private final T value;

	public PublishSignal(T value) {
		this.value = value;
	}

	public T value() {
		return value;
	}
}
