package com.github.tix320.kiwi.observable.signal;

import java.util.Objects;

import static com.github.tix320.kiwi.observable.signal.SignalPriorities.PUBLISH_PRIORITY;

public non-sealed class PublishSignal<T> extends Signal {

	private final T item;

	public PublishSignal(T item) {
		this.item = Objects.requireNonNull(item, "Published item must not be null");
	}

	public final T getItem() {
		return item;
	}

	@Override
	public int priority() {
		return PUBLISH_PRIORITY;
	}

}
